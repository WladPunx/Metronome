package com.wladkoshelev.metronome.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.PlayListData
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

class PlayListsVM {

    fun params() = parametersOf()

    fun mModule() = module {
        viewModel<VM> {
            VM(
                songREP = get<SongREP.Face> { SongREP().params() }
            )
        }
    }

    class VM(
        private val songREP: SongREP.Face
    ) : ViewModel() {
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

        data class State(
            val allPlayLists: List<PlayListData> = emptyList()
        )

        private val _state = MutableStateFlow(State())
        val state = _state.asStateFlow()

        sealed interface Event {
            class CreateNewPlatList : Event
            class EditPlayList(val playListID : String) : Event
        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            class CreateNewPlayList() : Intent
            class EditPlayList(val playListID: String) : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.CreateNewPlayList -> _event.emit(Event.CreateNewPlatList())
                is Intent.EditPlayList -> _event.emit(Event.EditPlayList(intent.playListID))
            }
        }

        // all songs listener
        init {
            mScope.launch {
                songREP.allPlayLists.collect { list ->
                    _state.update { it.copy(allPlayLists = list) }
                }
            }

        }
    }
}