package com.wladkoshelev.metronome.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class AllSongsVM {

    fun params() = parametersOf()

    fun mModule() = module {
        viewModel<VM> {
            VM(
                songRep = get<SongREP.Face> { SongREP().params() }
            )
        }
    }

    class VM(
        private val songRep: SongREP.Face
    ) : ViewModel() {
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

        data class State(
            val songsList: List<SongData> = emptyList()
        )

        private val _state = MutableStateFlow(State())
        val state = _state.asStateFlow()

        sealed interface Event {
            data class NavigateToMetronomeWithSong(val songId: String) : Event
        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            data class SongClick(val song: SongData) : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SongClick -> _event.emit(Event.NavigateToMetronomeWithSong(intent.song.id))
            }
        }

        // all songs flow listener
        init {
            mScope.launch {
                songRep.allSongs.collect { newList ->
                    _state.update { it.copy(songsList = newList) }
                }
            }
        }

    }
}