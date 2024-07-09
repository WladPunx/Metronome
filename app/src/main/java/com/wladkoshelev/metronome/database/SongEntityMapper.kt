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
        fun mapEntityToData(entity: SongEntity): SongData
        fun mapDataToEntity(data: SongData): SongEntity
    }

    class Impl : Face {

        override fun mapEntityToData(entity: SongEntity) = entity.toData()
        private fun SongEntity.toData() = SongData(
            id = id,
            name = name,
            speed = speed,
            tactSize = tactSize
        )


        override fun mapDataToEntity(data: SongData) = data.toEntity()
        private fun SongData.toEntity() = SongEntity(
            id = id,
            name = name,
            speed = speed,
            tactSize = tactSize
        )

    }
}