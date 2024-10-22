package com.wladkoshelev.metronome.metronome

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.ext.koin.androidApplication
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
                apl = androidApplication()
            )
        }
    }

    interface Face {
        fun start()
        fun stop()
        fun setTactSize(tactSize: Int)
        fun setBmp(bmp: Int)
        val state: StateFlow<MetronomeStateData>
        val soundNameList: List<String>
        fun setMainSound(title: String)
        fun setSecondSound(title: String)
    }

    class Impl(
        apl: Application
    ) : Face {

        /** ШаредПреф настроек звуков Метранома */
        private val soundSharedPref = apl.getSharedPreferences("MetronomeSoundSettings", Context.MODE_PRIVATE)

        /** звук Сильной Доли */
        private var mainSound = soundSharedPref.getString(MAIN_SOUND, null).let { param ->
            MetronomeSoundEntity.values().find { it.title == param } ?: MetronomeSoundEntity.values().first()
        }

        /** звук Слабой доли */
        private var secondSound = soundSharedPref.getString(SECOND_SOUND, null).let { param ->
            MetronomeSoundEntity.values().find { it.title == param } ?: MetronomeSoundEntity.values()[1]
        }

        /** полный список доступных звуков. только имена {[MetronomeSoundEntity.title]}, т.к. надо отдать Domain модель наружу */
        override val soundNameList = MetronomeSoundEntity.values().map { it.title }

        private val _state = MutableStateFlow(MetronomeStateData())
        override val state = _state.asStateFlow()

        /** дэфолтная установка названия звуков в стейт Метранома */
        init {
            _state.update {
                it.copy(
                    mainSoundName = mainSound.title,
                    secondSoundName = secondSound.title
                )
            }
        }

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
                            if (state.currentBeatCount == MetronomeStateData.START_BEAT) mainSound.sound else secondSound.sound,
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


        /** установка звука Сильной доли */
        override fun setMainSound(title: String) {
            MetronomeSoundEntity.values().find {
                it.title == title
            }?.let { sound ->
                if (sound != mainSound) {
                    _state.update {
                        soundSharedPref
                            .edit()
                            .putString(MAIN_SOUND, sound.title)
                            .apply()
                        mainSound = sound
                        it.copy(mainSoundName = sound.title)
                    }
                }
            }
        }

        /** установка звука Слабой доли */
        override fun setSecondSound(title: String) {
            MetronomeSoundEntity.values().find {
                it.title == title
            }?.let { sound ->
                if (sound != secondSound) {
                    _state.update {
                        soundSharedPref
                            .edit()
                            .putString(SECOND_SOUND, sound.title)
                            .apply()
                        secondSound = sound
                        it.copy(secondSoundName = sound.title)
                    }
                }
            }
        }


        companion object {
            /** константы для {[soundSharedPref]} */
            private const val MAIN_SOUND = "MAIN_SOUND"
            private const val SECOND_SOUND = "SECOND_SOUND"
        }


    }
}