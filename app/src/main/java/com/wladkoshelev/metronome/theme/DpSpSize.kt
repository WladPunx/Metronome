package com.wladkoshelev.metronome.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** стиль закругления кнопок для всего приложения */
val ButtonShape = RoundedCornerShape(14.dp)

/** отступ от низа для контроль панели */
val BottomControlPadding = PaddingValues(bottom = 15.dp)

/** размер иконок для контролпанели, которая находится снизу. связан с {[BottomControlPadding]} */
val BottomControlButtonSize = 30.dp