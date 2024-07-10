package com.wladkoshelev.metronome.database

import androidx.room.TypeConverter
import com.wladkoshelev.metronome.utils.GsonUtil.fromJson
import com.wladkoshelev.metronome.utils.GsonUtil.toJson

class SongDBConverter {

    @TypeConverter
    fun toJsonListStrings(list: List<String?>?): String {
        return list.toJson()
    }

    @TypeConverter
    fun fromJsonStringLIst(json: String?): List<String?>? {
        return json?.fromJson()
    }

}