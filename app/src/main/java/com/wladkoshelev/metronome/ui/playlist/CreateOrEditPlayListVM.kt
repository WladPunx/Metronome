package com.wladkoshelev.metronome.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.PlayListData
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.UUID

class CreateOrEditPlayListVM {

    fun params(playListID: String?) = parametersOf(playListID)

    fun mModule() = module {
        viewModel<VM> { (playListID: String) ->
            VM(
                songREP = get<SongREP.Face> { SongREP().params() },
                id = playListID
            )
        }
    }

    class VM(
        private val songREP: SongREP.Face,
        val id: String?
    ) : ViewModel() {
        private val mDispatcher = Dispatchers.IO
        private val mScope = viewModelScope + mDispatcher

        data class State(
            val songsWithCheck: List<Pair<SongData, Boolean>> = emptyList(),
            val name: String = "",
            val playListId: String
        )

        private val _state = MutableStateFlow(
            State(
                playListId = id ?: UUID.randomUUID().toString()
            )
        )
        val state = _state.asStateFlow()

        sealed interface Event {

        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            data class CheckUnCheckSong(val song: Pair<SongData, Boolean>) : Intent
            data class SetName(val name: String) : Intent
            class SavePlayList() : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.CheckUnCheckSong -> _state.update {
                    val newList = it.songsWithCheck.toMutableList()
                    newList.remove(intent.song)
                    newList.add(Pair(intent.song.first, intent.song.second.not()))
                    it.copy(songsWithCheck = newList.sortedBy { !it.second })
                }

                is Intent.SetName -> _state.update { it.copy(name = intent.name) }
                is Intent.SavePlayList -> mScope.launch {
                    songREP.savePlayList(
                        _state.value.let {
                            PlayListData(
                                id = it.playListId,
                                name = it.name,
                                songsIdList = it.songsWithCheck.filter { it.second }.map { it.first }
                            )
                        }
                    )
                }
            }
        }

        // init all songs
        init {
            mScope.launch {
                val currentPlayListFromDB = songREP.allPlayLists.map {
                    it.find { it.id == _state.value.playListId }
                }.firstOrNull()

                val playListSong = (currentPlayListFromDB?.songsIdList ?: emptyList())

                val mAllSongs = (songREP.allSongs.firstOrNull() ?: emptyList()).map {
                    Pair(it, playListSong.contains(it))
                }.sortedBy { it.second.not() }

                _state.update {
                    it.copy(
                        songsWithCheck = mAllSongs,
                        name = currentPlayListFromDB?.name.orEmpty()
                    )
                }
            }
        }
    }
}