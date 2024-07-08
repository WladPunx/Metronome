package com.wladkoshelev.metronome

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.util.Calendar
import java.util.Date
import java.util.Timer
import kotlin.concurrent.timerTask

class MetronomeLDS {

    fun params() = parametersOf()

    fun mModule() = module {
        single<Face> {
            Impl(

            )
        }
    }

    interface Face {
        fun start(beatsPerMinute: Int, tactSize: Int)
        fun stop()
    }

    class Impl : Face {

        private val mScope = MainScope() + Dispatchers.IO
        private val timerList = MutableStateFlow(listOf<Timer>())

        override fun stop() {
            timerList.update {
                it.forEach { it.cancel() }
                emptyList()
            }
        }

        override fun start(
            beatsPerMinute: Int,
            tactSize: Int
        ) {
            stop()
            setNewTact(
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100),
                startTime = Calendar.getInstance().time.time + 300,
                beatDelay = (1000 * 60 / beatsPerMinute).toLong(),
                tactSize = tactSize
            )
        }


        private fun setNewTact(
            toneGenerator: ToneGenerator,
            startTime: Long,
            beatDelay: Long,
            tactSize: Int
        ) {
            mScope.launch {
                val tactTimes = (0 until tactSize).map {
                    startTime + (it * beatDelay)
                }
                tactTimes.forEachIndexed { index, d ->
                    val timer = Timer("${d}", true)
                    timerList.update {
                        it.toMutableList().apply {
                            add(timer)
                        }
                    }
                    timer.schedule(
                        timerTask {
                            toneGenerator.startTone(
                                if (index == 0) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_PIP,
                                150
                            )
                            timerList.update {
                                it.toMutableList().apply {
                                    remove(timer)
                                }
                            }
                            if (index == 0) {
                                setNewTact(
                                    toneGenerator = toneGenerator,
                                    startTime = tactTimes.last() + beatDelay,
                                    beatDelay = beatDelay,
                                    tactSize = tactSize
                                )
                            }
                        },
                        Date(d)
                    )
                }
            }
        }

    }

}