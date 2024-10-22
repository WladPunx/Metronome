package com.wladkoshelev.metronome.metronome

/** модель состояния метранома для {[MetronomeLDS.Face]}, но является Domain-моделью
 *
 * корректировки по мин/макс встроены по умолчанию */
data class MetronomeStateData(
    /** кол-во ударов в минуту */
    private val _bmp: Int = 120,
    /** кол-во ударов. первый удар - сильная доля */
    private val _tactSize: Int = 4,
    /** время следующего удара */
    val nextBeatTime: Long = 0,
    /** статус проигрывания */
    val isPlay: Boolean = false,
    /** текущий удар в такте */
    private val _currentBeatCount: Int = START_BEAT,
    /** название звука для Сильной доли */
    val mainSoundName: String = "",
    /** название звука Слабой доли */
    val secondSoundName: String = ""
) {

    val tactSize = _tactSize.coerceIn(MIN_TACT_SIZE, MAX_TACT_SIZE)
    val bmp: Int = _bmp.coerceIn(MIN_SPEED, MAX_SPEED)

    /** задержка между ударами в мс */
    val beatDelay = (1000 * 60 / bmp).toLong()

    val currentBeatCount = _currentBeatCount.let {
        if (it > tactSize || it < START_BEAT) START_BEAT else it
    }

    /** константы для корректировки {[bmp]} {[tactSize]]
     *
     * должны быть публичными, чтобы можно было создать UI с указанием мин/макс значений из этих констант */
    companion object {
        /** индекс подсчета сильной доли
         *
         * используется для проверки {[MetronomeLDS.Impl.setNextBeat]} и корректировки {[currentBeatCount]} */
        const val START_BEAT = 1

        const val MIN_SPEED = 30
        const val MAX_SPEED = 245

        const val MIN_TACT_SIZE = 2
        const val MAX_TACT_SIZE = 8
    }

}