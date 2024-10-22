package com.wladkoshelev.metronome.metronome

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

class MetronomeREP {

    fun params() = parametersOf()

    fun mModule() = module {
        factory<Face> {
            Impl(
                metronomeLDS = get<MetronomeLDS.Face> { MetronomeLDS().params() }
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
        private val metronomeLDS: MetronomeLDS.Face
    ) : Face {
        override val state = metronomeLDS.state
        override val soundNameList = metronomeLDS.soundNameList

        override fun start() {
            metronomeLDS.start()
        }

        override fun stop() {
            metronomeLDS.stop()
        }

        override fun setTactSize(tactSize: Int) {
            metronomeLDS.setTactSize(tactSize)
        }

        override fun setBmp(bmp: Int) {
            metronomeLDS.setBmp(bmp)
        }

        override fun setMainSound(title: String) {
            metronomeLDS.setMainSound(title)
        }

        override fun setSecondSound(title: String) {
            metronomeLDS.setSecondSound(title)
        }
    }
}