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

        /** запуск проигрывания метранома с отложенным временем {[MetronomeStateData.nextBeatTime]} */
        override fun start() {
            _state.update {
                if (it.isPlay) return
                it.copy(
                    isPlay = true,
                    nextBeatTime = Calendar.getInstance().time.time + 300,
                    _currentBeatCount = MetronomeStateData.START_BEAT
                )
            }
            setNextBeat()
        }

        /** установка размера такта. корректируется внутри модели самостоятельно */
        override fun setTactSize(tactSize: Int) {
            _state.update { it.copy(_tactSize = tactSize) }
        }

        /** устанвока скорости.корректируется внутри модели самостоятельно. подсчет задержки следующего бита внутри модели */
        override fun setBmp(bmp: Int) {
            _state.update { it.copy(_bmp = bmp) }
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
         * после его воспроизведения, делаем перерасчет {[MetronomeStateData.currentBeatCount]} и {[MetronomeStateData.nextBeatTime]} и опять вызывает этот метод
         *
         * {[MetronomeStateData.currentBeatCount]} корректируется внутри модели */
        private fun setNextBeat() {
            Timer(true).schedule(
                timerTask {
                    _state.update { state ->
                        if (state.isPlay.not()) return@timerTask

                        toneGenerator.startTone(
                            if (state.currentBeatCount == MetronomeStateData.START_BEAT) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_PIP,
                            150
                        )

                        state.copy(
                            _currentBeatCount = state.currentBeatCount.inc(),
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