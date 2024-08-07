package com.wladkoshelev.metronome.metronome

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class MetronomeLDS {

    fun params() = parametersOf()

    fun mModule() = module {
        factory<Face> {
            Impl(

            )
        }
    }

    interface Face {
        fun start()
        fun stop()
        fun setTactSize(tactSize: Int)
        fun setBmp(bmp: Int)
        val state: StateFlow<MetronomeStateData>
    }

    class Impl : Face {

        private val _state = MutableStateFlow(MetronomeStateData())
        override val state = _state.asStateFlow()

        /** вызов методов, чтобы произвести корректировку данных и подсчитать задержку между битами */
        init {
            _state.value.let {
                setTactSize(it.tactSize)
                setBmp(it.bmp)
            }
        }


        /** запуск проигрывания метранома с отложенным временем {[MetronomeStateData.nextBeatTime]} */
        override fun start() {
            _state.update {
                if (it.isPlay) return
                it.copy(
                    isPlay = true,
                    nextBeatTime = Calendar.getInstance().time.time + 300,
                    currentBeatCount = 1
                )
            }
            setNextBeat()
        }

        /** установка размера такта с корректировкой по мин/максу */
        override fun setTactSize(tactSize: Int) {
            _state.update { it.copy(tactSize = tactSize.coerceIn(MetronomeStateData.MIN_TACT_SIZE, MetronomeStateData.MAX_TACT_SIZE)) }
        }

        /** устанвока скорости с подсчетом задержки до следующего бита */
        override fun setBmp(bmp: Int) {
            val newBmp = bmp.coerceIn(MetronomeStateData.MIN_SPEED, MetronomeStateData.MAX_SPEED)
            _state.update {
                it.copy(
                    bmp = newBmp,
                    beatDelay = (1000 * 60 / newBmp).toLong()
                )
            }
        }

        /** остановка проигрывания */
        override fun stop() {
            _state.update { it.copy(isPlay = false) }
        }

        private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        /** установка таймера для проигрывания следующего бита
         *
         * таймер устанавливается на заренее подсчитанное время {[MetronomeStateData.nextBeatTime]}
         *
         * после его воспроизведения, делаем перерасчет {[MetronomeStateData.currentBeatCount]} и {[MetronomeStateData.nextBeatTime]} и опять вызывает этот метод */
        private fun setNextBeat() {
            Timer(true).schedule(
                timerTask {
                    _state.update { state ->
                        if (state.isPlay.not()) return@timerTask

                        toneGenerator.startTone(
                            if (state.currentBeatCount == 1) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_PIP,
                            150
                        )

                        state.copy(
                            currentBeatCount = state.currentBeatCount.inc().let {
                                if (it > state.tactSize) 1 else it
                            },
                            nextBeatTime = state.nextBeatTime + state.beatDelay
                        )
                    }
                    setNextBeat()
                },
                Date(_state.value.nextBeatTime)
            )
        }


    }
}