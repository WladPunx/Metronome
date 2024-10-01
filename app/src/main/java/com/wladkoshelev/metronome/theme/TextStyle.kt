package com.wladkoshelev.metronome.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.R

val mainFont = FontFamily(Font(R.font.main))


val MainTextStyle = TextStyle(
    color = MainTextColor,
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    fontFamily = mainFont
)

val SecondTextStyle = MainTextStyle.copy(
    color = SecondTextColor,
    fontSize = 16.sp,
    fontStyle = FontStyle.Italic,
    fontWeight = null
)

val EmptyListTextStyle = SecondTextStyle.copy(
    textAlign = TextAlign.Center,
    fontSize = 20.sp,
    color = MainTextColor
)


val AlertDialogTitleStyle = MainTextStyle
val AlertDialogTextStyle = MainTextStyle.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)
