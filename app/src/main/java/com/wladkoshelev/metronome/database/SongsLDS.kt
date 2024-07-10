package com.wladkoshelev.metronome.database

import com.wladkoshelev.metronome.utils.flow.FlowShareWhileSubscribed.shareWhileSubscribed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.Calendar

class SongsLDS {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Face> {
            val dao = get<SongsDB.Dao> { SongsDB().params() }
            val mapper = get<SongEntityMapper.Face> { SongEntityMapper().params() }
            Impl(
                ucAllSongs = dao.getAll(),
                ucSaveSong = dao::save,
                ucDataToEntity = mapper::dataToEntity,
                ucEntityToData = mapper::entityToData,
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
        private val ucDataToEntity: (SongData, Long) -> SongEntity,
        private val ucDelete: (SongEntity) -> Unit,
        private val ucGetSongById: (id: String) -> Flow<SongEntity?>
    ) : Face {

        override val allSongs = ucAllSongs.map {
            it
                .sortedByDescending { it.date }
                .map { ucEntityToData(it) }
        }.shareWhileSubscribed()

        override suspend fun saveSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            val songSaveDate = ucAllSongs.firstOrNull()?.find { it.id == song.id }?.date ?: Calendar.getInstance().time.time
            ucSaveSong(ucDataToEntity(song, songSaveDate))
        }

        override suspend fun deleteSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            ucDelete(ucDataToEntity(song, 0))
        }

        override fun getSongById(id: String) = ucGetSongById(id)
            .map {
                it?.let(ucEntityToData)
            }
            .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .distinctUntilChanged()

    }
}