package com.wladkoshelev.metronome.ui.tap_the_beat

import com.wladkoshelev.metronome.utils.MDispatchers
import com.wladkoshelev.metronome.utils.SafeScope.toSafeScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/** компонента для Настукивание Бита {[TapTheBeatView]} */
class TapTheBeatComponent(
    scope: CoroutineScope
) {
    /** колбэк дял применения результата. автоматически НЕ закрывает БоттомШит, чтобы была возможность валидировать результат.
     * (хотя хз для чего это может быть нужно, но пусть будет) */
    var applyBeatValue: (Int) -> Unit = {}

    private val mScope = scope.toSafeScope(MDispatchers.IO)

    data class State(
        /** показывать Модалку? */
        val isShow: Boolean = false,
        /** результат скорости настукивания */
        val speed: Int = 0,
        /** список времени (мс) когда были сделаны удары */
        val beatList: List<Long> = emptyList(),
        /** работает ли запись? не пустой ли массив {[beatList]}? он очищается в методе {[clearBeatListWithDelay]} */
        val isWork: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    sealed interface Intent {
        /** показывать модалку? */
        data class IsShow(val isShow: Boolean) : Intent

        /** удар */
        class BeatIt() : Intent

        /** применить результат {[State.speed]} */
        class ApplyNewBeat() : Intent
    }

    fun sendIntent(intent: Intent) {
        when (intent) {
            is Intent.IsShow -> _state.update {
                /** останавливаем метод по очистке {[State.beatList]}
                 * сбрасываем стейт в дэфолтное состояние */
                clearBeatListJob?.cancel()
                State().copy(isShow = intent.isShow)
            }

            is Intent.BeatIt -> _state.update {
                /** добавляем текущее время и удаляем старые, чтобы список времени был актуальным */
                val newBeatList = it.beatList.toMutableList().apply {
                    add(Calendar.getInstance().time.time)
                    while (size > MAX_BEAT_LIST_SIZE) {
                        removeFirst()
                    }
                }

                /** будущий результат */
                var speedResult: Long = 0

                /** кол-во подсчитанной разницы ударов для вычисления среднего значения скорости */
                var beatDiffSize = 0
                if (newBeatList.size >= 2) {
                    /** перебираем массив. если есть следующее значение, то вычитаем разницу их и увеличиваем {[beatDiffSize]} */
                    newBeatList.forEachIndexed { index, current ->
                        newBeatList.getOrNull(index + 1)?.let { next ->
                            speedResult += next - current
                            beatDiffSize++
                        }
                    }
                    /** находим среднее значение на основе {[beatDiffSize]} */
                    speedResult /= beatDiffSize
                    /** преобразуем среднее значение в BPM */
                    speedResult = 60_000 / speedResult
                }
                clearBeatListWithDelay()
                it.copy(
                    beatList = newBeatList,
                    speed = speedResult.toInt(),
                    isWork = true
                )
            }

            is Intent.ApplyNewBeat -> applyBeatValue(_state.value.speed)
        }
    }


    /** метод для очистки массива времени ударов, если ударов нет определенное времени */
    private var clearBeatListJob: Job? = null
    private fun clearBeatListWithDelay() {
        clearBeatListJob?.cancel()
        clearBeatListJob = mScope.launch {
            delay(CLEAR_BEAT_TIME)
            _state.update { it.copy(beatList = emptyList(), isWork = false) }
        }
    }

    companion object {
        private const val MAX_BEAT_LIST_SIZE = 6
        private const val CLEAR_BEAT_TIME = 3_000L
    }
}