package com.wladkoshelev.metronome.ui.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.database.SongSaveStatus
import com.wladkoshelev.metronome.metronome.MetronomeREP
import com.wladkoshelev.metronome.metronome.MetronomeStateData
import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
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

    fun params(songId: String?) = parametersOf(songId)

    fun mModule() = module {
        viewModel<VM> { (songID: String?) ->
            VM(
                songRep = get<SongREP.Face> { SongREP().params() },
                metronomeREP = get<MetronomeREP.Face> { MetronomeREP().params() },
                songId = songID
            )
        }
    }

    class VM(
        private val metronomeREP: MetronomeREP.Face,
        private val songRep: SongREP.Face,
        songId: String?,
    ) : ViewModel() {
        private val mScope = viewModelScope.toSafeScope(MDispatchers.IO)

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
            val isShowExitWithoutSaveAlert: Boolean = false,
            /** показывать Алерт удаления песни? */
            val isShowDeleteAlert: Boolean = false
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

            /** сохранить и выйти с фрагмента */
            class SaveAndExit() : Intent

            /** выйти без сохранения */
            class ExitWithoutSave() : Intent
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
                is Intent.OnBackClick -> _state.update {
                    val isShowExitAlert = it.isCanSave && (it.songName.isNotEmpty() || it.isCanDelete)
                    if (isShowExitAlert.not()) _event.emit(Event.OnBack())
                    it.copy(isShowExitWithoutSaveAlert = isShowExitAlert)
                }

                is Intent.SaveAndExit -> mScope.launch {
                    saveSong()
                    _state.update { it.copy(isShowExitWithoutSaveAlert = false) }
                    if (_state.value.saveStatus == SongSaveStatus.SUCCESS) {
                        _event.emit(Event.OnBack())
                    }
                }

                is Intent.ExitWithoutSave -> _event.emit(Event.OnBack())
                is Intent.IsShowDeleteAlert -> _state.update { it.copy(isShowDeleteAlert = intent.isShow) }
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

        override fun onCleared() {
            metronomeREP.stop()
            super.onCleared()
        }

    }
}