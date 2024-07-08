package com.wladkoshelev.metronome.utils


import android.util.Log
import com.wladkoshelev.metronome.BuildConfig
import java.util.UUID


private val isLogEnable = BuildConfig.DEBUG
private const val TAG = "MetronomeTAG"
const val LOG_INFO_DEFAULT_PART = 2
const val LOG_ERROR_DEFAULT_PART = 3
const val LOG_ERROR_DEFAULT_PART_ERROR = 0


fun logInfo(txt: String, part: Int = LOG_INFO_DEFAULT_PART) {
    if (isLogEnable) {
        val cti = Throwable().stackTrace.getOrNull(part)
        val str =
            "${cti?.methodName} / (${cti?.fileName}:${cti?.lineNumber}) : ${txt}\n${UUID.randomUUID()}"
        runCatching {
            Log.e(TAG, str)
        }.getOrElse {
            println("${TAG}: ${str}")
        }
    }
}


fun Throwable.logError(part: Int = LOG_ERROR_DEFAULT_PART, errorPart: Int = LOG_ERROR_DEFAULT_PART_ERROR) {
    if (isLogEnable) {
        val error = this.stackTrace.getOrNull(errorPart)
        logInfo(
            "ERROR: ${this.javaClass.canonicalName}: ${this.message} \n ${error}",
            part
        )

        val str = this.stackTraceToString()
        runCatching {
            Log.i(TAG, str)
        }.getOrElse {
            println("$TAG: $str")
        }
    }
}


fun logUtilsTest() {
    runCatching { 10 / 0 }.getOrElse {
        logInfo("1111")
        logInfo("22222", LOG_INFO_DEFAULT_PART)
        it.logError()
        it.logError(LOG_ERROR_DEFAULT_PART)
        it.logError(LOG_ERROR_DEFAULT_PART, LOG_ERROR_DEFAULT_PART_ERROR)
    }
}






