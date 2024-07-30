package com.wladkoshelev.metronome.utils

import kotlinx.coroutines.Dispatchers

/** {[MDispatchers]} - это объект для поставки {[Dispatchers]}, которые можно заменить, в случае создания юнит тестов
 *
 * тогда необходимо создать метод для установки и вызывать приватные сеттеры
 *
 * SonarLint kotlin:S6310
 *
 * https://developer.android.com/kotlin/coroutines/coroutines-best-practices#inject-dispatchers */
object MDispatchers {

    var Main = Dispatchers.Main
        private set

    var IO = Dispatchers.IO
        private set

    var Default = Dispatchers.Default
        private set

}