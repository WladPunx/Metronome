package com.wladkoshelev.metronome.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListFragment
import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class SongsVM {

    fun params(playListID: String?) = parametersOf(playListID)

    fun mModule() = module {
        viewModel<VM> { (playListID: String?) ->
            VM(
                songRep = get<SongREP.Face> { SongREP().params() },
                playListID = playListID
            )
        }
    }

    class VM(
        private val songRep: SongREP.Face,
        private val playListID: String?
    ) : ViewModel() {
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

        /** фрагмент используется для отображения списка песен.
         * могут быть как Все песни приложения, так и лишь песни выбранного Плейлиста */
        data class State(
            /** список песен */
            val songsList: List<SongData> = emptyList(),
            /** имя плейлиста. если это все песни, то значение == null */
            val playListName: String? = null,
            /** режим Все Песни или это Плейлист */
            val mode: Mode = Mode.ALL_SONG
        ) {
            /** основные отличия режимов:
             * * показывать ли нумерацию песен? показывать, если это Плейлист. если Все песни - не показывать
             * * разные кнопки управления фрагментом */
            enum class Mode {
                ALL_SONG,
                PLAYLIST;
            }
        }

        private val _state = MutableStateFlow(State())
        val state = _state.asStateFlow()

        sealed interface Event {
            /** перейти в Метроном с выбранной песней и ИД плейлиста. доступно для всех режимов */
            data class NavigateToMetronome(val songId: String?, val playListID: String?) : Event

            /** редактировать Плейлист. доступно только для Плейлиста */
            data class NavigateToEditPlayList(val playListID: String) : Event

            /** событие Назад. сработает в случае, если {[playListID]} != null, но при прослушивании плейлиста из БД, вернулся null.
             * это значит что плейлист был удален на следующем экране {[CreateOrEditPlayListFragment]} и на этом экране нам больше нечего делать  */
            class OnBack() : Event
        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            /** клик на элемент списка. доступно для всех и переведет нас в Метроном через {[Event.NavigateToMetronome]} */
            data class SongClick(val song: SongData?) : Intent

            /** клик на редактирование Плейлиста */
            class EditPlayListClick() : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SongClick -> _event.emit(Event.NavigateToMetronome(songId = intent.song?.id, playListID = playListID))
                is Intent.EditPlayListClick -> playListID?.let {
                    _event.emit(Event.NavigateToEditPlayList(it))
                }
            }
        }

        /** прослушивание БД для получения списка песен */
        init {
            mScope.launch {
                /** если {[playListID]}==null то мы слушаем все песни из БД */
                if (playListID == null) {
                    songRep.allSongs.collect { newList ->
                        _state.update { it.copy(songsList = newList, playListName = null, mode = State.Mode.ALL_SONG) }
                    }
                } else {
                    /** если {[playListID]}!=null, то слушаем конкретный плейлист
                     * если в процессе прослушивания мы сколлечиваем null, значит плейлист был удален на следующем экране {[CreateOrEditPlayListFragment]}
                     * и тут нам делать больше нечего и мы отправляем {[Event.OnBack]}*/
                    songRep.allPlayLists
                        .map { it.find { it.id == playListID } }
                        .collect { mPlayList ->
                            if (mPlayList == null) {
                                _event.emit(Event.OnBack())
                            } else {
                                _state.update { it.copy(songsList = mPlayList.songsList, playListName = mPlayList.name, mode = State.Mode.PLAYLIST) }
                            }
                        }
                }
            }
        }

    }
}