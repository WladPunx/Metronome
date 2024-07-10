package com.wladkoshelev.metronome.ui.metronome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wladkoshelev.metronome.MetronomeLDS
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.utils.flow.SingleFlowEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
                metronomeLDS = get<MetronomeLDS.Face> { MetronomeLDS().params() },
                songId = songID
            )
        }
    }

    class VM(
        private val metronomeLDS: MetronomeLDS.Face,
        private val songRep: SongREP.Face,
        private val songId: String?,
    ) : ViewModel() {
        private val mDispatcher = Dispatchers.IO
        private val mScope = viewModelScope + mDispatcher

        data class State(
            val speed: Int? = 100,
            val tactSize: Int? = 4,
            val songId: String,
            val songName: String = "",
            val isShowSave: Boolean = false,
            val isShowDelete: Boolean = false
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
            data class SetSpeed(val speed: Int?) : Intent
            data class SetSTactSize(val tactSize: Int?) : Intent
            class Play : Intent
            class Stop : Intent
            class NoFocus : Intent
            data class SetName(val name: String) : Intent
            class SaveSong : Intent
            class DeleteSong : Intent
        }

        fun sendIntent(intent: Intent) {
            when (intent) {
                is Intent.SetSpeed -> _state.update { it.copy(speed = intent.speed) }
                is Intent.SetSTactSize -> _state.update { it.copy(tactSize = intent.tactSize) }

                is Intent.Play -> {
                    coerceMetronome()
                    val mSpeed = _state.value.speed
                    val mTactSize = _state.value.tactSize
                    if (mSpeed != null && mTactSize != null) {
                        metronomeLDS.start(mSpeed, mTactSize)
                    }
                }

                is Intent.NoFocus -> coerceMetronome()
                is Intent.Stop -> metronomeLDS.stop()
                is Intent.SetName -> _state.update { it.copy(songName = intent.name) }

                is Intent.SaveSong -> mScope.launch {
                    coerceMetronome()
                    songRep.saveSong(getSongDataFromState())
                }

                is Intent.DeleteSong -> mScope.launch {
                    songRep.deleteSong(getSongDataFromState())
                }
            }
        }

        private fun coerceMetronome() {
            _state.update {
                it.copy(
                    speed = (it.speed ?: 0).coerceIn(MIN_SPEED, MAX_SPEED),
                    tactSize = (it.tactSize ?: 0).coerceIn(MIN_TACT_SIZE, MAX_TACT_SIZE)
                )
            }
        }

        private val currentSongFromDB = _state.map { it.songId }.distinctUntilChanged()
            .flatMapLatest {
                songRep.allSongs.map {
                    it.find { it.id == songId }
                }
            }.stateIn(mScope, SharingStarted.Eagerly, null)

        // listener current song from db
        init {
            mScope.launch {
                currentSongFromDB.collect { dbSong ->
                    _state.update {
                        it.copy(
                            speed = dbSong?.speed ?: it.speed,
                            tactSize = dbSong?.tactSize ?: it.tactSize,
                            songName = dbSong?.name ?: it.songName
                        )
                    }
                }
            }
        }

        // show save/delete listener
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

        private fun getSongDataFromState() = _state.value.let {
            SongData(
                id = it.songId,
                name = it.songName,
                speed = it.speed ?: 0,
                tactSize = it.tactSize ?: 0
            )
        }

        override fun onCleared() {
            metronomeLDS.stop()
            super.onCleared()
        }

        companion object {
            private const val MIN_SPEED = 30
            private const val MAX_SPEED = 245

            private const val MIN_TACT_SIZE = 2
            private const val MAX_TACT_SIZE = 8
        }
    }
}