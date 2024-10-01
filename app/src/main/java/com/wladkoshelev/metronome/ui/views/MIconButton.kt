package com.wladkoshelev.metronome.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
 * @param isEnable в зависимости от этого параметра меняется дизайн и сработает ли {[onClick]}
 * @param iconSize размер иконки
 * @param iconRes иконка кнопки */
@Composable
@Preview
fun MIconButton(
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.ic_save,
    iconSize: Dp = 20.dp,
    isEnable: Boolean = true,
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isPressed: Boolean = interactionSource.collectIsPressedAsState().value
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
            .scale(animateFloatAsState(targetValue = if (isPressed) 1.5f else 1f, label = "").value)
            .clip(ButtonShape)
            .background(
                if (isPressed.not()) {
                    DefaultButtonColor.copy(
                        alpha = if (isEnable) 1f else 0.5f
                    )
                } else {
                    ButtonTextColor
                }
            )
            .clickable(
                enabled = isEnable,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
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
                if (isPressed.not()) {
                    ButtonTextColor.copy(
                        alpha = if (isEnable) 1f else 0.5f
                    )
                } else {
                    DefaultButtonColor
                }

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
            .background(Color.Green)
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

        MIconButton(
            modifier = Modifier
                .align(Alignment.End),
            iconSize = 32.dp,
            isPressed = true
        )
        Text(text = "нажатая")
    }
}