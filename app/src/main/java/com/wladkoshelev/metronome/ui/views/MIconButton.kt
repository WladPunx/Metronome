package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.theme.ButtonShape
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DefaultButtonColor

/** кнопочная иконка для всего приложения
 *
 * @param onClick обработка клика на кнопку
 * @param isEnable в зависимости от этого параметра меняется дизайн и сработает ли {[onClick]}  */
@Composable
@Preview
fun MIconButton(
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.ic_save,
    iconSize: Dp = 20.dp,
    isEnable: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
            .clip(ButtonShape)
            .background(
                DefaultButtonColor.copy(
                    alpha = if (isEnable) 1f else 0.5f
                )
            )
            .clickable(
                enabled = isEnable,
                onClick = onClick
            )
            .padding(7.dp)
    ) {
        Image(
            modifier = Modifier
                .size(iconSize)
                .align(Alignment.Center),
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                ButtonTextColor.copy(
                    alpha = if (isEnable) 1f else 0.5f
                )
            )
        )
    }
}

@Composable
@Preview
private fun Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        MIconButton()
        Text(text = "дэфолтная")

        MIconButton(isEnable = false)
        Text(text = "выключенная")

        MIconButton(modifier = Modifier.fillMaxWidth())
        Text(text = "на всю ширину")

        MIconButton(iconSize = 60.dp)
        Text(text = "большой размер иконки")

        MIconButton(iconSize = 15.dp)
        Text(text = "маленький размер иконки")
    }
}