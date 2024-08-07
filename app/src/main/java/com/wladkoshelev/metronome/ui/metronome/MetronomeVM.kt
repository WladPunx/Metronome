package com.wladkoshelev.metronome.ui.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
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
            val isShowSave: Boolean = false,
            /** можно ли удалить песню? удалить песню, можно если она сохранена в БД */
            val isShowDelete: Boolean = false,
            /** статус АлертДиалога для редактирования Bmp */
            val isShowEditBmp: Boolean = false,
            /** статус АлертДиалога для редактирования размера такта */
            val isShowEditTactSize: Boolean = false
        )

        private val _state = MutableStateFlow(
            State(
                songId = songId ?: UUID.randomUUID().toString()
            )
        )
        val state = _state.asStateFlow()

        sealed interface Event {

        }

        private val _event = SingleFlowEvent<Event>(mScope)
        val event = _event.flow

        sealed interface Intent {
            data class SetSpeed(val speed: Int) : Intent
            data class SetSTactSize(val tactSize: Int) : Intent
            class Play : Intent
            class Stop : Intent
            data class SetName(val name: String) : Intent
            class SaveSong : Intent
            class DeleteSong : Intent
            data class IsShowEditBmp(val isShow: Boolean) : Intent
            data class IsShowEditTactSize(val isShow: Boolean) : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SetSpeed -> metronomeREP.setBmp(intent.speed)
                is Intent.SetSTactSize -> metronomeREP.setTactSize(intent.tactSize)
                is Intent.Play -> metronomeREP.start()
                is Intent.Stop -> metronomeREP.stop()
                is Intent.SetName -> _state.update { it.copy(songName = intent.name) }
                is Intent.SaveSong -> mScope.launch {
                    songRep.saveSong(getSongDataFromState())
                }

                is Intent.DeleteSong -> mScope.launch {
                    songRep.deleteSong(getSongDataFromState())
                }

                is Intent.IsShowEditBmp -> _state.update { it.copy(isShowEditBmp = intent.isShow) }
                is Intent.IsShowEditTactSize -> _state.update { it.copy(isShowEditTactSize = intent.isShow) }
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
                            isShowSave = result.first != result.second,
                            isShowDelete = result.second != null
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

        private fun getSongDataFromState() = _state.value.let {
            SongData(
                id = it.songId,
                name = it.songName,
                speed = it.metronomeState.bmp,
                tactSize = it.metronomeState.tactSize
            )
        }

        override fun onCleared() {
            metronomeREP.stop()
            super.onCleared()
        }

    }
}