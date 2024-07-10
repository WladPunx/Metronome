package com.wladkoshelev.metronome.database

import android.app.Application
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import org.koin.android.ext.koin.androidApplication
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongsDB {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Dao> { DB.get(androidApplication()).dao() }
    }

    @androidx.room.Dao
    interface Dao {
        /**
         * Song
         */
        @Query("select * from songs_table")
        fun getAllSongs(): Flow<List<SongEntity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun saveSongs(song: SongEntity)

        @Delete
        fun deleteSongs(song: SongEntity)

        /**
         * PlayList
         */
        @Query("select * from play_list_table")
        fun getAllPlayList(): Flow<List<PlayListEntity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun savePlayList(playList: PlayListEntity)
    }

    @Database(
        entities = [SongEntity::class, PlayListEntity::class], version = 1, exportSchema = true, autoMigrations = [

        ]
    )
    @TypeConverters(SongDBConverter::class)
    abstract class DB : RoomDatabase() {
        abstract fun dao(): Dao

        companion object {
            fun get(apl: Application) = Room.databaseBuilder(
                apl,
                DB::class.java,
                "song_db_1"
            )
                .fallbackToDestructiveMigrationOnDowngrade()
                .fallbackToDestructiveMigrationFrom(
                    // drop versions
                )
                .build()
        }
    }
}