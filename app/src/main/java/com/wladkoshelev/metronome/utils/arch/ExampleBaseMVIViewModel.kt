package com.wladkoshelev.metronome.utils.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class ExampleBaseMVIViewModel {

    fun params() = parametersOf()

    fun mModule() = module {
        viewModel<VM> {
            VM(

            )
        }
    }

    class VM : ViewModel() {
        private val mDispatcher = Dispatchers.IO
        private val mScope = viewModelScope + mDispatcher

        data class State(
            val a: String = ""
        )

        private val _state = MutableStateFlow(State())
        val state = _state.asStateFlow()
        fun <T> mapState(mMap: suspend (State) -> T) = state.map(mMap).distinctUntilChanged()

        sealed interface Event {

        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            class A : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.A -> {}
            }
        }
    }
}