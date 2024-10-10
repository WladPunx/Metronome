package com.wladkoshelev.metronome.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListFragment
import com.wladkoshelev.metronome.ui.songs.SongsFragment

/** стиль закругления кнопок для всего приложения */
val ButtonShape = RoundedCornerShape(14.dp)

/** отступ от низа для контроль панели */
val BottomControlPadding = PaddingValues(bottom = 15.dp)

/** размер иконок для контролпанели, которая находится снизу. связан с {[BottomControlPadding]} */
val BottomControlButtonSize = 30.dp

/** отступы и дайвайдеры для списков песен в {[SongsFragment]} {[CreateOrEditPlayListFragment]} */
val ListElementHorizontalPadding = PaddingValues(horizontal = 15.dp)
val ListElementDividerSize = 0.5.dp
val ListElementDividerPadding = 10.dp


/** шейп бэкграунда для Алертов */
val AlertDialogShape = ButtonShape

/** внутренние отступы от краев для Алерта */
val AlertDialogInnerPadding = PaddingValues(20.dp)