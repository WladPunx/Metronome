package com.wladkoshelev.metronome.database

import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope
import com.wladkoshelev.metronome.utils.flow.FlowShareWhileSubscribed.shareWhileSubscribed
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
                songDao = get<SongsDB.Dao> { SongsDB().params() }
            )
        }
    }

    interface Face {
        val allSongs: SharedFlow<List<SongData>>
        suspend fun saveSong(song: SongData)
        suspend fun deleteSong(song: SongData)
        suspend fun savePlayList(playList: PlayListData)
        val allPlayList: SharedFlow<List<PlayListData>>
        suspend fun deletePlayList(playList: PlayListData)
    }

    class Impl(
        private val songDao: SongsDB.Dao
    ) : Face {

        private val mScope = SafeScope.get()

        /** список всех песен с учетом времени их создания. время создания указывается в {[saveSong]} */
        override val allSongs = songDao.getAllSongs().map {
            it
                .sortedByDescending { it.date }
                .map { it.toData() }
        }.shareWhileSubscribed(mScope)


        /** сохранение песни с учетом времени время.
         *
        нужно для сортировки песен в {[allSongs]} */
        override suspend fun saveSong(song: SongData): Unit = withContext(MDispatchers.IO) {
            val songSaveDate = songDao.getAllSongs().firstOrNull()?.find { it.id == song.id }?.date ?: Calendar.getInstance().time.time
            songDao.saveSongs(song.toEntity(songSaveDate))
        }

        /** удаление песни по ID */
        override suspend fun deleteSong(song: SongData): Unit = withContext(MDispatchers.IO) {
            songDao.deleteSong(song.id)
        }


        /** список всех плейлистов на основе данных из БД и {[allSongs]}
         *
        в БД для плейлиста сохраняется лишь ID'шник песни {[SongData.id]} {[PlayListEntity.songsIdList]} */
        override val allPlayList = songDao.getAllPlayList().combine(allSongs) { listPlaylist, allSong ->
            listPlaylist.map {
                PlayListData(
                    name = it.name,
                    id = it.id,
                    songsList = mutableListOf<SongData>().apply {
                        it.songsIdList.forEach { songId ->
                            allSong.find { it.id == songId }?.let { add(it) }
                        }
                    }
                )
            }
        }.shareWhileSubscribed(mScope)

        /** сохранение плейлиста */
        override suspend fun savePlayList(playList: PlayListData): Unit = withContext(MDispatchers.IO) {
            songDao.savePlayList(
                PlayListEntity(
                    id = playList.id,
                    name = playList.name,
                    songsIdList = playList.songsList.map { it.id }
                )
            )
        }

        /** удаление плейлиста по ID */
        override suspend fun deletePlayList(playList: PlayListData): Unit = withContext(MDispatchers.IO) {
            songDao.deletePlayList(playList.id)
        }

        /** Data to Entity
         *
         * с сохранением даты, используемой для сортировки в {[allSongs]} {[saveSong]} */
        private fun SongData.toEntity(date: Long) = SongEntity(
            id = id,
            name = name,
            speed = speed,
            tactSize = tactSize,
            date = date
        )

        /** Entity to Data */
        private fun SongEntity.toData() = SongData(
            id = id,
            name = name,
            speed = speed,
            tactSize = tactSize,
        )

    }
}