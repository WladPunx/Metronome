package com.wladkoshelev.metronome.utils.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn

object FlowShareWhileSubscribed {

    /** преобразование холодного Flow в горячий со стратегий WhileSubscribed
     *
     * пока у него есть подписчики - он работает.
     *
     * когда все подписчики исчезнут, то черзе 30 секунд преобразование холодного в горчий остановится
     *
     * кэш сбросится сразу после остановки */
    fun <T> Flow<T>.shareWhileSubscribed(scope: CoroutineScope) = this
        .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(1000 * 30, 0),
            replay = 1
        )
}