package com.wladkoshelev.metronome.ui_metranome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.MetronomeLDS
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class MetronomeVM {

    fun params() = parametersOf()

    fun mModule() = module {
        viewModel<VM> {
            val metronomeLDS = get<MetronomeLDS.Face> { MetronomeLDS().params() }
            VM(
                ucPlay = metronomeLDS::start,
                ucStop = metronomeLDS::stop
            )
        }
    }

    class VM(
        private val ucPlay: (beatsPerMinute: Int, tactSize: Int) -> Unit,
        private val ucStop: () -> Unit
    ) : ViewModel() {
        private val mDispatcher = Dispatchers.IO
        private val mScope = viewModelScope + mDispatcher

        data class State(
            val speed: Int? = 100,
            val tactSize: Int? = 4,
            val isPlay: Boolean = false
        )

        private val _state = MutableStateFlow(State())
        val state = _state.asStateFlow()

        sealed interface Event {

        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            data class SetSpeed(val speed: Int?) : Intent
            data class SetSTactSize(val tactSize: Int?) : Intent
            class Play : Intent
            class NoFocus : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SetSpeed -> {
                    _state.update { it.copy(speed = intent.speed) }
                }

                is Intent.SetSTactSize -> {
                    _state.update { it.copy(tactSize = intent.tactSize) }
                }

                is Intent.Play -> {
                    coerceMetronome()
                    val mSpeed = _state.value.speed
                    val mTactSize = _state.value.tactSize
                    if (mSpeed != null && mTactSize != null) {
                        ucPlay(mSpeed, mTactSize)
                    }
                }

                is Intent.NoFocus -> coerceMetronome()
            }
        }

        private fun coerceMetronome() {
            _state.update {
                it.copy(
                    speed = (it.speed ?: 0).coerceIn(MIN_SPEED, MAX_SPEED),
                    tactSize = (it.tactSize ?: 0).coerceIn(MIN_TACT_SIZE, MAX_TACT_SIZE)
                )
            }
        }

        override fun onCleared() {
            ucStop()
            super.onCleared()
        }

        companion object {
            private const val MIN_SPEED = 30
            private const val MAX_SPEED = 245

            private const val MIN_TACT_SIZE = 2
            private const val MAX_TACT_SIZE = 8
        }
    }
}