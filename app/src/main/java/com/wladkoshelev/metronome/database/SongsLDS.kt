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

        /** список всех песен с учетом времени их создания. время создания указывается в {[saveSong]} */
        override val allSongs = songDao.getAllSongs().map {
            it
                .sortedByDescending { it.date }
                .map { mapper.entityToData(it) }
        }.shareWhileSubscribed()


        /** сохранение песни с учетом времени время.
         *
        нужно для сортировки песен в {[allSongs]} */
        override suspend fun saveSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            val songSaveDate = songDao.getAllSongs().firstOrNull()?.find { it.id == song.id }?.date ?: Calendar.getInstance().time.time
            songDao.saveSongs(mapper.dataToEntity(song, songSaveDate))
        }

        /** удаление песни */
        override suspend fun deleteSong(song: SongData): Unit = withContext(Dispatchers.IO) {
            songDao.deleteSongs(mapper.dataToEntity(song, 0))
        }


        /** список всех плейлистов на основе данных из БД и {[allSongs]}
         *
        в БД для плейлиста сохраняется лишь ID'шник песни {[SongData.id]} {[PlayListEntity.songsIdList]} */
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

        /** сохранение плейлиста */
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