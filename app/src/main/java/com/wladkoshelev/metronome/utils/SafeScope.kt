package com.wladkoshelev.metronome.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object SafeScope {

    /** создание безопасного скоупа, защенного от внутренних ошибок и падений
     *
     * все скоупы необходимо использовать с этим методом. */
    fun CoroutineScope.toSafeScope(context: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
        var result = this.coroutineContext + context
        result += SupervisorJob(result[Job])
        if (result[CoroutineExceptionHandler] == null) {
            result += CoroutineExceptionHandler { coroutineContext, throwable -> throwable.logError() }
        }
        return CoroutineScope(result)
    }


    fun get(context: CoroutineContext = EmptyCoroutineContext) = MainScope().toSafeScope(MDispatchers.IO + context)

}