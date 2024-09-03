package com.wladkoshelev.metronome.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.PlayListData
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

        data class State(
            /** выбранные песни в Плейлисте */
            val selectSong: List<SongData> = emptyList(),
            /** остальные песни */
            val unSelectSong: List<SongData> = emptyList(),
            /** название плейлиста */
            val name: String = "",
            /** ИД плейлиста */
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
            /** клик на песню, по которому она добавляет или удаляется из плейлиста */
            data class CheckUnCheckSong(val song: SongData) : Intent

            /** ввод имени плейлиста */
            data class SetName(val name: String) : Intent

            /** сохранение плейлиста */
            class SavePlayList() : Intent

            /** передвижение элементов в списке */
            data class OnMoveSelectSong(val from: Int, val to: Int) : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.CheckUnCheckSong -> _state.update {
                    val mSelectSong = it.selectSong.toMutableList()
                    val mUnselectSong = it.unSelectSong.toMutableList()

                    if (mSelectSong.contains(intent.song)) {
                        mSelectSong.remove(intent.song)
                        mUnselectSong.add(intent.song)
                    } else {
                        mSelectSong.add(intent.song)
                        mUnselectSong.remove(intent.song)
                    }
                    it.copy(
                        selectSong = mSelectSong,
                        unSelectSong = mUnselectSong
                    )
                }

                is Intent.SetName -> _state.update { it.copy(name = intent.name) }
                is Intent.SavePlayList -> mScope.launch {
                    songREP.savePlayList(
                        _state.value.let {
                            PlayListData(
                                id = it.playListId,
                                name = it.name,
                                songsIdList = it.selectSong
                            )
                        }
                    )
                }

                is Intent.OnMoveSelectSong -> {
                    _state.update {
                        it.copy(
                            selectSong = it.selectSong.toMutableList().apply {
                                add(intent.to, removeAt(intent.from))
                            }
                        )
                    }
                }
            }
        }

        /** инициализация начальных списков для Выбранных Песен и Остальных */
        init {
            mScope.launch {
                val currentPlayListFromDB = songREP.allPlayLists.map {
                    it.find { it.id == _state.value.playListId }
                }.firstOrNull()

                val playListSong = currentPlayListFromDB?.songsIdList.orEmpty()
                val mUnSelectSongs = mutableListOf<SongData>()

                songREP.allSongs.firstOrNull().orEmpty().forEach {
                    if (!playListSong.contains(it)) {
                        mUnSelectSongs.add(it)
                    }
                }

                _state.update {
                    it.copy(
                        selectSong = playListSong,
                        unSelectSong = mUnSelectSongs,
                        name = currentPlayListFromDB?.name.orEmpty()
                    )
                }
            }
        }
    }
}