package com.wladkoshelev.metronome

import android.media.AudioManager
import android.media.ToneGenerator
import com.wladkoshelev.metronome.utils.SafeScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

        private val mScope = SafeScope.get()

        /** список заготовленных таймеров (текущий и следующий такт)
         *
        наполняется в методе {[setNewTact]}

        отменяются и очищаются в методе {[stop]} */
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
                            /** проигрывание бита должно быть первым. все остальные вычисления - после */
                            toneGenerator.startTone(
                                if (index == 0) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_CDMA_PIP,
                                150
                            )
                            /** удаление текущего таймера из {[timerList]}, чтобы поддерживать в списке только актуальные таймеры */
                            timerList.update {
                                it.toMutableList().apply {
                                    remove(timer)
                                }
                            }
                            /** генерация следующего такта, если текущий бит является сильной долей
                            по итогу в {[timerList]} будет находится текущий такст + следующий */
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