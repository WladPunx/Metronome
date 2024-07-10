package com.wladkoshelev.metronome.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class AllSongsVM {

    fun params() = parametersOf()

    fun mModule() = module {
        viewModel<VM> {
            val rep = get<SongREP.Face> { SongREP().params() }
            VM(
                ucAllSongs = rep.allSongs
            )
        }
    }

    class VM(
        private val ucAllSongs: Flow<List<SongData>>
    ) : ViewModel() {
        private val mDispatcher = Dispatchers.IO
        private val mScope = viewModelScope + mDispatcher

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
                ucAllSongs.collect { newList ->
                    _state.update { it.copy(songsList = newList) }
                }
            }
        }

    }
}