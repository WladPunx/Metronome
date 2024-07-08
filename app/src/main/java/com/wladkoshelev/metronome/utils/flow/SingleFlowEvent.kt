package com.wladkoshelev.metronome.utils.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SingleFlowEvent<T> constructor(private val scope: CoroutineScope) {
    private val channel = Channel<T>(Channel.UNLIMITED)
    val flow = channel.receiveAsFlow()
    fun emit(value: T) {
        scope.launch {
            channel.send(value)
        }
    }
}