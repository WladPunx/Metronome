package com.wladkoshelev.metronome.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object GsonUtil {
    val gson = Gson()
    inline fun <reified T> type(): Type = object : TypeToken<T>() {}.type
    inline fun <reified T> T.toJson(): String = gson.toJson(this, type<T>())
    inline fun <reified T> String.fromJson(): T = gson.fromJson(this, type<T>())
    inline fun <reified T> T.copyAny(): T = this.toJson().fromJson()
}