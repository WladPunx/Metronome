package com.wladkoshelev.metronome.database

import com.wladkoshelev.metronome.utils.flow.FlowShareWhileSubscribed.shareWhileSubscribed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongsLDS {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Face> {
            val dao = get<SongsDB.Dao> { SongsDB().params() }
            val mapper = get<SongEntityMapper.Face> { SongEntityMapper().params() }
            Impl(
                ucAllSongs = dao.getAll(),
                ucSaveSong = dao::save,
                ucDataToEntity = mapper::mapDataToEntity,
                ucEntityToData = mapper::mapEntityToData,
                ucDelete = dao::delete,
                ucGetSongById = dao::getSongByID
            )
        }
    }

    interface Face {
        val allSongs: SharedFlow<List<SongData>>
        suspend fun saveSong(song: SongData)
        suspend fun deleteSong(song: SongData)
        fun getSongById(id: String): Flow<SongData?>
    }

    class Impl(
        private val ucAllSongs: Flow<List<SongEntity>>,
        private val ucSaveSong: (song: SongEntity) -> Unit,
        private val ucEntityToData: (SongEntity) -> SongData,
        private val ucDataToEntity: (SongData) -> SongEntity,
        private val ucDelete: (SongEntity) -> Unit,
        private val ucGetSongById: (id: String) -> Flow<SongEntity?>
    ) : Face {

        override val allSongs = ucAllSongs.map {
            it.map {
                ucEntityToData(it)
            }
        }.shareWhileSubscribed()

        override suspend fun saveSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            ucSaveSong(ucDataToEntity(song))
        }

        override suspend fun deleteSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            ucDelete(ucDataToEntity(song))
        }

        override fun getSongById(id: String) = ucGetSongById(id)
            .map {
                it?.let(ucEntityToData)
            }
            .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .distinctUntilChanged()

    }
}