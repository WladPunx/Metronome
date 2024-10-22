package com.wladkoshelev.metronome.ui.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.database.SongSaveStatus
import com.wladkoshelev.metronome.metronome.MetronomeREP
import com.wladkoshelev.metronome.metronome.MetronomeStateData
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.State
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.UUID

class MetronomeVM {

    fun params(songId: String?, playListID: String?) = parametersOf(songId, playListID)

    fun mModule() = module {
        viewModel<VM> { (songID: String?, playListID: String?) ->
            VM(
                songRep = get<SongREP.Face> { SongREP().params() },
                metronomeREP = get<MetronomeREP.Face> { MetronomeREP().params() },
                songId = songID,
                playListID = playListID
            )
        }
    }


    /** ВМ работает за счет передачи в нее ИДшника песни и Плейлиста
     *
     * если ИД песни == null, то это просто заход в метраном. у {[_state]} поле ИДшника {[State.songId]} будет сгенерено случайно
     *
     * от ИД песни работают слушатели БД, которые понимают "есть ли песня с таким ИД в БД?
     * есть ли изменения в настройках метранома от того, что есть БД?"
     *
     * {[playListID]} нужен чтобы следить за следующим и предыдущим треком в плейлисте. если он ==null, то значит мы выбрали песню вне плейлиста */
    class VM(
        private val metronomeREP: MetronomeREP.Face,
        private val songRep: SongREP.Face,
        songId: String?,
        private val playListID: String?
    ) : ViewModel() {
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

        /** модель для Алерта про выход без сохранения */
        data class AlertModel(
            /** показывать Алерт? */
            val isShow: Boolean,
            /** действие при "Сохранить и выйти" */
            val onSuccess: () -> Unit,
            /** действие при Закрыть */
            val onCancelClick: () -> Unit
        ) {
            companion object {
                /** пустая модель для скрытия */
                val None = AlertModel(false, {}, {})
            }
        }

        data class State(
            /** состояние метранома */
            val metronomeState: MetronomeStateData = MetronomeStateData(),
            /** ID песни. он или передан, если это уже существующая песня, либо создаться случайно */
            val songId: String,
            /** имя песни, с которой она будет сохранена в БД */
            val songName: String = "",
            /** можно ли сохранить песню в БД или она ничем не отличается */
            val isCanSave: Boolean = false,
            /** можно ли удалить песню? удалить песню, можно если она сохранена в БД */
            val isCanDelete: Boolean = false,
            /** статус АлертДиалога для редактирования Bmp */
            val isShowEditBmp: Boolean = false,
            /** статус АлертДиалога для редактирования размера такта */
            val isShowEditTactSize: Boolean = false,
            /** статус сохранения песни для отображения ошибок. если ==Success, то ошибка не показывается */
            val saveStatus: SongSaveStatus = SongSaveStatus.SUCCESS,
            /** показывать Алерт для выхода без сохранения? */
            val isShowExitWithoutSaveAlert: AlertModel = AlertModel.None,
            /** показывать Алерт удаления песни? */
            val isShowDeleteAlert: Boolean = false,
            /** предыдущая песня, если есть */
            val previousSong: SongData? = null,
            /** предыдущая песня, если есть */
            val nextSong: SongData? = null
        )

        private val _state = MutableStateFlow(
            State(
                songId = songId ?: UUID.randomUUID().toString()
            )
        )
        val state = _state.asStateFlow()

        sealed interface Event {
            /** событие выхода с экрана */
            class OnBack() : Event
        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            /** установка скорости песни */
            data class SetSpeed(val speed: Int) : Intent

            /** установка размера такта */
            data class SetSTactSize(val tactSize: Int) : Intent

            /** проигрывать метраном */
            class Play : Intent

            /** останвоить проигрывание */
            class Stop : Intent

            /** ввод имени песни */
            data class SetName(val name: String) : Intent

            /** клик на сохранение песни */
            class SaveSong : Intent

            /** удаление песни из БД */
            class DeleteSong : Intent

            /** показывать Алерт для ручного ввода скорости? */
            data class IsShowEditBmp(val isShow: Boolean) : Intent

            /** показывать Алерт для ручного ввода размера такта? */
            data class IsShowEditTactSize(val isShow: Boolean) : Intent

            /** клик Назад */
            class OnBackClick() : Intent

            /** показывать алерт удаления песни? */
            data class IsShowDeleteAlert(val isShow: Boolean) : Intent

            /** клик на следующую песню в плейлисте */
            class NextSongClick() : Intent

            /** клик на предыдущую песню в плейлисте */
            class PreviosSongClick() : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SetSpeed -> metronomeREP.setBmp(intent.speed)
                is Intent.SetSTactSize -> metronomeREP.setTactSize(intent.tactSize)
                is Intent.Play -> metronomeREP.start()
                is Intent.Stop -> metronomeREP.stop()
                is Intent.SetName -> _state.update { it.copy(songName = intent.name) }
                is Intent.SaveSong -> mScope.launch { saveSong() }
                is Intent.DeleteSong -> mScope.launch {
                    songRep.deleteSong(getSongDataFromState())
                    _event.emit(Event.OnBack())
                }

                is Intent.IsShowEditBmp -> _state.update { it.copy(isShowEditBmp = intent.isShow) }
                is Intent.IsShowEditTactSize -> _state.update { it.copy(isShowEditTactSize = intent.isShow) }
                is Intent.OnBackClick -> checkSave { _event.emit(Event.OnBack()) }
                is Intent.IsShowDeleteAlert -> _state.update { it.copy(isShowDeleteAlert = intent.isShow) }
                is Intent.NextSongClick -> _state.value.nextSong?.let { nextSong ->
                    checkSave {
                        _state.update { it.copy(songId = nextSong.id) }
                    }
                }

                is Intent.PreviosSongClick -> _state.value.previousSong?.let { previosSong ->
                    checkSave {
                        _state.update { it.copy(songId = previosSong.id) }
                    }
                }
            }
        }


        /** текущий трек из БД по ИД */
        private val currentSongFromDB = _state.map { it.songId }.distinctUntilChanged()
            .flatMapLatest { mSongID ->
                songRep.allSongs.map {
                    it.find { it.id == mSongID }
                }
            }.stateIn(mScope, SharingStarted.Eagerly, null)

        /** слушатель инфы о песни из БД */
        init {
            mScope.launch {
                currentSongFromDB.mapNotNull { it }.collect { dbSong ->
                    _state.update { it.copy(songName = dbSong.name) }
                    metronomeREP.setBmp(dbSong.speed)
                    metronomeREP.setTactSize(dbSong.tactSize)
                }
            }
        }

        /** логика отображение элементов Сохранить / Удалить */
        init {
            mScope.launch {
                _state.combine(currentSongFromDB) { state, dbSong ->
                    Pair(getSongDataFromState(), dbSong)
                }.collect { result ->
                    _state.update {
                        it.copy(
                            isCanSave = result.first != result.second,
                            isCanDelete = result.second != null
                        )
                    }
                }
            }
        }

        /** прослушивание стейта метронома */
        init {
            mScope.launch {
                metronomeREP.state.collect { newState ->
                    _state.update { it.copy(metronomeState = newState) }
                }
            }
        }

        /** метод формирования {[SongData]} на основе стейта */
        private fun getSongDataFromState() = _state.value.let {
            SongData(
                id = it.songId,
                name = it.songName,
                speed = it.metronomeState.bmp,
                tactSize = it.metronomeState.tactSize
            )
        }

        /** сохранение песни в БД */
        private suspend fun saveSong(): Unit = withContext(MDispatchers.IO) {
            val saveStatus = songRep.saveSong(getSongDataFromState())
            _state.update { it.copy(saveStatus = saveStatus) }
        }


        /** приватный метод для проверки "Надо ли показывать о несохраненных изменениях?"
         *
         * {[invoke]} - это логика, что надо сделать, после успешного сохранения или после отказа от сохранения */
        private fun checkSave(
            invoke: () -> Unit
        ) {
            _state.update {
                val isShowExitAlert = it.isCanSave && (it.songName.isNotEmpty() || it.isCanDelete)
                if (isShowExitAlert.not()) {
                    invoke()
                    return
                    /** обязательно делать ретурн, иначе может быть баг: рандомные показы о сохранении, когда нет изменений */
                }
                it.copy(isShowExitWithoutSaveAlert = AlertModel(
                    isShow = isShowExitAlert,
                    onSuccess = {
                        mScope.launch {
                            _state.update { it.copy(isShowExitWithoutSaveAlert = AlertModel.None) }
                            saveSong()
                            if (_state.value.saveStatus == SongSaveStatus.SUCCESS) {
                                invoke()
                            }
                        }
                    },
                    onCancelClick = {
                        _state.update { it.copy(isShowExitWithoutSaveAlert = AlertModel.None) }
                        invoke()
                    }
                ))
            }
        }


        /** слушатель для подготовки следующией и предыдущей песен */
        init {
            songRep.allPlayLists
                .mapNotNull { it.find { it.id == playListID } }
                .combine(currentSongFromDB) { playList, song ->
                    val currentIndex = playList.songsList.indexOf(song)
                    val previousSong = playList.songsList.getOrNull(currentIndex - 1)
                    val nextSong = playList.songsList.getOrNull(currentIndex + 1)
                    _state.update {
                        it.copy(
                            previousSong = previousSong,
                            nextSong = nextSong
                        )
                    }
                }
                .launchIn(mScope)
        }

        override fun onCleared() {
            metronomeREP.stop()
            super.onCleared()
        }

    }
}