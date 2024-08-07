package com.wladkoshelev.metronome.metronome

/** модель состояния метранома для {[MetronomeLDS.Face]}, но является Domain-моделью */
data class MetronomeStateData(
    /** кол-во ударов в минуту */
    val bmp: Int = 120,
    /** кол-во ударов. первый удар - сильная доля */
    val tactSize: Int = 4,
    /** задержка между ударами в мс */
    val beatDelay: Long = 0,
    /** время следующего удара */
    val nextBeatTime: Long = 0,
    /** статус проигрывания */
    val isPlay: Boolean = false,
    /** текущий удар в такте */
    val currentBeatCount: Int = 1
) {

    /** константы для корректировки {[bmp]} {[tactSize]]
     *
     * используются в {[MetronomeLDS.Impl.setBmp]} {[MetronomeLDS.Impl.setTactSize]}
     *
     * должны быть публичными, чтобы можно было создать UI с указанием мин/макс значений из этих констант */
    companion object {
        const val MIN_SPEED = 30
        const val MAX_SPEED = 245

        const val MIN_TACT_SIZE = 2
        const val MAX_TACT_SIZE = 8
    }

}