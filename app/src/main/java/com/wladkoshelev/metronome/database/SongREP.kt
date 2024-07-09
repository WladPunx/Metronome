package com.wladkoshelev.metronome.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongREP {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Face> {
            val lds = get<SongsLDS.Face> { SongsLDS().params() }
            Impl(
                ucAllSongs = lds.allSongs,
                ucSaveSong = lds::saveSong,
                ucDeleteSong = lds::deleteSong,
                ucGetSongById = lds::getSongById
            )
        }
    }

    interface Face {
        val allSongs: SharedFlow<List<SongData>>
        suspend fun saveSong(song: SongData): SongSaveStatus
        suspend fun deleteSong(song: SongData)
        fun getSongById(id: String): Flow<SongData?>
    }

    class Impl(
        private val ucAllSongs: SharedFlow<List<SongData>>,
        private val ucSaveSong: suspend (SongData) -> Unit,
        private val ucDeleteSong: suspend (SongData) -> Unit,
        private val ucGetSongById: (id: String) -> Flow<SongData?>
    ) : Face {

        override val allSongs = ucAllSongs

        override suspend fun saveSong(song: SongData): SongSaveStatus = withContext(Dispatchers.IO) {
            val namesList = (allSongs.firstOrNull() ?: emptyList())
                .filterNot {
                    it.id == song.id
                }
                .map {
                    it.name.filterNot { it.isWhitespace() }
                }
            val mName = song.name.filterNot { it.isWhitespace() }

            return@withContext when {
                mName.isEmpty() -> SongSaveStatus.EMPTY_NAME
                namesList.contains(mName) -> SongSaveStatus.NAME_EXIST
                else -> {
                    ucSaveSong(song)
                    SongSaveStatus.SUCCESS
                }
            }
        }

        override suspend fun deleteSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            ucDeleteSong(song)
        }

        override fun getSongById(id: String) = ucGetSongById(id)

    }
}