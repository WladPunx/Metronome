package com.wladkoshelev.metronome.utils.flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus

object FlowShareWhileSubscribed {

    private val mScope = MainScope() + Dispatchers.IO

    fun <T> Flow<T>.shareWhileSubscribed() = this
        .buffer(onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .distinctUntilChanged()
        .shareIn(mScope, SharingStarted.WhileSubscribed(1000 * 30, 0), 1)

}