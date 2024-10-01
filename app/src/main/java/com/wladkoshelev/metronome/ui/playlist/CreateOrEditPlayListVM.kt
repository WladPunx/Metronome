package com.wladkoshelev.metronome.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.PlayListData
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.database.SongSaveStatus
import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            val playListId: String,
            /** есть отличия от плейлиста, что хранится в БД? если да - то можно сохранить плейлист */
            val isCanSave: Boolean = false,
            /** этот плейлист есть в БД? значит его можно удалить */
            val isCanDelete: Boolean = false,
            /** статус сохронения плейлиста для отображения ошибки. если Успешный - ошибки нет */
            val saveStatus: SongSaveStatus = SongSaveStatus.SUCCESS,
            /** показ модалки по добавлению песен */
            val isShowAllSongs: Boolean = false,
            /** модалка для удаления плейлиста */
            val isShowDeleteAlertDialog: Boolean = false,
            /** модалка для выхода, когда есть несохраненые изменеия */
            val isShowExitDialog: Boolean = false
        )

        private val _state = MutableStateFlow(
            State(
                playListId = id ?: UUID.randomUUID().toString()
            )
        )
        val state = _state.asStateFlow()

        sealed interface Event {
            /** выход с экрана */
            class OnBack() : Event
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

            /** удаления плейлиста */
            class DeletePlayList() : Intent

            /** показывать/скрывать модалку про удаление плейлиста */
            data class IsShowDeleteAlert(val isShow: Boolean) : Intent

            /** передвижение элементов в списке */
            data class OnMoveSelectSong(val from: Int, val to: Int) : Intent

            /** показывать/скрывать модалку для добавления песен */
            data class SetIsShowAllSong(val isShow: Boolean) : Intent

            /** событий клика "назад" */
            class OnBackPressed() : Intent

            /** выход без сохранения */
            class ExitWithoutSave() : Intent

            /** сохранить и выйти */
            class SaveAndExit() : Intent

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

                is Intent.SetName -> _state.update { it.copy(name = intent.name, saveStatus = SongSaveStatus.SUCCESS) }
                is Intent.SavePlayList -> mScope.launch { savePlayList() }

                is Intent.OnMoveSelectSong -> {
                    _state.update {
                        it.copy(
                            selectSong = it.selectSong.toMutableList().apply {
                                add(intent.to, removeAt(intent.from))
                            }
                        )
                    }
                }

                is Intent.DeletePlayList -> mScope.launch {
                    songREP.deletePlayList(_state.value.getPlayListDataFromState())
                    _event.emit(Event.OnBack())
                }

                is Intent.SetIsShowAllSong -> _state.update { it.copy(isShowAllSongs = intent.isShow) }
                is Intent.IsShowDeleteAlert -> _state.update { it.copy(isShowDeleteAlertDialog = intent.isShow) }

                is Intent.OnBackPressed -> _state.update {
                    val result = it.copy(isShowExitDialog = it.isCanSave)
                    if (result.isShowExitDialog.not()) _event.emit(Event.OnBack())
                    result
                }

                is Intent.ExitWithoutSave -> _event.emit(Event.OnBack())
                is Intent.SaveAndExit -> {
                    mScope.launch {
                        savePlayList()
                        _state.update { it.copy(isShowExitDialog = false) }
                        if (_state.value.saveStatus == SongSaveStatus.SUCCESS) {
                            _event.emit(Event.OnBack())
                        }
                    }
                }
            }
        }

        /** создание модели плейлиста на основе стейта экрана */
        private fun State.getPlayListDataFromState() = PlayListData(
            id = playListId,
            name = name,
            songsList = selectSong
        )


        /** текущий плейлист из БД */
        private val currentPlayListFromDB = _state
            .map { it.playListId }
            .distinctUntilChanged()
            .flatMapLatest { id ->
                songREP.allPlayLists.map {
                    it.find { it.id == id }
                }
            }
            .stateIn(mScope, SharingStarted.Eagerly, null)


        /** слушатель для выбранных и невыбранных песен, на основе плейлиста из БД и списка всех песен */
        init {
            songREP.allSongs
                .combine(currentPlayListFromDB) { allSong, playList ->
                    val playListSong = playList?.songsList.orEmpty()
                    val mUnSelectSongs = mutableListOf<SongData>()

                    allSong.forEach {
                        if (!playListSong.contains(it)) {
                            mUnSelectSongs.add(it)
                        }
                    }

                    _state.update {
                        it.copy(
                            selectSong = playListSong,
                            unSelectSong = mUnSelectSongs,
                            name = playList?.name.orEmpty()
                        )
                    }
                }.launchIn(mScope)
        }

        /** сохранение плейлиста. используется для кнопок сохранения и при выходе с экрана с изменениями
         *
         * {[Intent.SaveAndExit]} {[Intent.SavePlayList]} */
        private suspend fun savePlayList(): Unit = withContext(MDispatchers.IO) {
            val status = songREP.savePlayList(
                _state.value.getPlayListDataFromState()
            )
            _state.update { it.copy(saveStatus = status) }
        }

        /** прослушивание текущего стейта и плейлиста из БД, чтобы понимать "можно ли сохранить/удалить этот плейлист?" */
        init {
            _state
                .combine(currentPlayListFromDB) { state, fromDB ->
                    _state.update {
                        it.copy(
                            isCanSave = state.getPlayListDataFromState() != fromDB,
                            isCanDelete = fromDB != null
                        )
                    }
                }
                .launchIn(mScope)
        }

    }
}