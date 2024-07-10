package com.wladkoshelev.metronome.database

import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongEntityMapper {

    fun params() = parametersOf()

    fun mModule() = module {
        factory<Face> {
            Impl(

            )
        }
    }

    interface Face {
        fun dataToEntity(data: SongData, date: Long): SongEntity
        fun entityToData(entity: SongEntity): SongData
    }

    class Impl : Face {

        override fun dataToEntity(data: SongData, date: Long) = SongEntity(
            id = data.id,
            name = data.name,
            speed = data.speed,
            tactSize = data.tactSize,
            date = date
        )

        override fun entityToData(entity: SongEntity) = SongData(
            id = entity.id,
            name = entity.name,
            speed = entity.speed,
            tactSize = entity.tactSize,
        )


    }
}