package com.wladkoshelev.metronome.database

import com.wladkoshelev.metronome.utils.MDispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongREP {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Face> {
            Impl(
                songLds = get<SongsLDS.Face> { SongsLDS().params() }
            )
        }
    }

    interface Face {
        val allSongs: SharedFlow<List<SongData>>
        suspend fun saveSong(song: SongData): SongSaveStatus
        suspend fun deleteSong(song: SongData)
        val allPlayLists: SharedFlow<List<PlayListData>>
        suspend fun savePlayList(playList: PlayListData)
    }

    class Impl(
        private val songLds: SongsLDS.Face
    ) : Face {

        override val allSongs = songLds.allSongs
        override val allPlayLists = songLds.allPlayList


        /** сохранение песни с валидацией [SongSaveStatus] **/
        override suspend fun saveSong(song: SongData): SongSaveStatus = withContext(MDispatchers.IO) {
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
                    songLds.saveSong(song)
                    SongSaveStatus.SUCCESS
                }
            }
        }

        override suspend fun deleteSong(song: SongData): Unit = withContext(MDispatchers.IO) {
            songLds.deleteSong(song)
        }

        override suspend fun savePlayList(playList: PlayListData): Unit = withContext(MDispatchers.IO) {
            songLds.savePlayList(playList)
        }

    }
}