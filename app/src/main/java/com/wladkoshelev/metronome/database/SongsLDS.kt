package com.wladkoshelev.metronome.database

import com.wladkoshelev.metronome.utils.flow.FlowShareWhileSubscribed.shareWhileSubscribed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
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
            Impl(
                songDao = get<SongsDB.Dao> { SongsDB().params() },
                mapper = get<SongEntityMapper.Face> { SongEntityMapper().params() }
            )
        }
    }

    interface Face {
        val allSongs: SharedFlow<List<SongData>>
        suspend fun saveSong(song: SongData)
        suspend fun deleteSong(song: SongData)
        suspend fun savePlayList(playList: PlayListData)
        val allPlayList: SharedFlow<List<PlayListData>>
    }

    class Impl(
        private val songDao: SongsDB.Dao,
        private val mapper: SongEntityMapper.Face
    ) : Face {

        /**
         * Songs
         */
        override val allSongs = songDao.getAllSongs().map {
            it
                .sortedByDescending { it.date }
                .map { mapper.entityToData(it) }
        }.shareWhileSubscribed()

        override suspend fun saveSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            val songSaveDate = songDao.getAllSongs().firstOrNull()?.find { it.id == song.id }?.date ?: Calendar.getInstance().time.time
            songDao.saveSongs(mapper.dataToEntity(song, songSaveDate))
        }

        override suspend fun deleteSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            songDao.deleteSongs(mapper.dataToEntity(song, 0))
        }


        /**
         * Playlist
         */

        override val allPlayList = songDao.getAllPlayList().combine(allSongs) { listPlaylist, allSong ->
            listPlaylist.map {
                PlayListData(
                    name = it.name,
                    id = it.id,
                    songsIdList = mutableListOf<SongData>().apply {
                        it.songsIdList.forEach { songId ->
                            allSong.find { it.id == songId }?.let { add(it) }
                        }
                    }
                )
            }
        }.shareWhileSubscribed()

        override suspend fun savePlayList(playList: PlayListData): Unit = withContext(Dispatchers.IO) {
            songDao.savePlayList(
                PlayListEntity(
                    id = playList.id,
                    name = playList.name,
                    songsIdList = playList.songsIdList.map { it.id }
                )
            )
        }

    }
}