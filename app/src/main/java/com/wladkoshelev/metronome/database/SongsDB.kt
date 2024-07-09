package com.wladkoshelev.metronome.database

import android.app.Application
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
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
        @Query("select * from songs_table")
        fun getAll(): Flow<List<SongEntity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun save(song: SongEntity)

        @Delete
        fun delete(song: SongEntity)

        @Query("select * from songs_table where id==:mId")
        fun getSongByID(mId: String): Flow<SongEntity?>
    }

    @Database(
        entities = [SongEntity::class], version = 1, exportSchema = true, autoMigrations = [

        ]
    )
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