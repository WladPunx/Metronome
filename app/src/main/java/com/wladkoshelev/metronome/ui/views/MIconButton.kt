package com.wladkoshelev.metronome.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.theme.ButtonShape
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.MainTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/** кнопочная иконка для всего приложения
 *
 * @param onClick обработка клика на кнопку
 * @param isEnable в зависимости от этого параметра меняется дизайн и сработает ли {[onClick]}
 * @param iconSize размер иконки
 * @param iconRes иконка кнопки
 * @param text текст кнопки
 * @param textSize размер текста, если он есть */
@Composable
@Preview
fun MIconButton(
    modifier: Modifier = Modifier,
    iconRes: Int? = R.drawable.ic_save,
    text: String? = "Button",
    iconSize: Dp = 20.dp,
    textSize: TextUnit = 20.sp,
    isEnable: Boolean = true,
    onClick: () -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isPressed: Boolean = interactionSource.collectIsPressedAsState().value
) {

    /** фон бэкграунда */
    val backgroundColor by remember(isPressed, isEnable) {
        mutableStateOf(
            if (isPressed.not()) {
                DefaultButtonColor.copy(
                    alpha = if (isEnable) 1f else 0.5f
                )
            } else {
                ButtonTextColor
            }
        )
    }

    /** цвет контента {[text]} {[iconRes]} */
    val contentColor by remember(isPressed, isEnable) {
        mutableStateOf(
            if (isPressed.not()) {
                ButtonTextColor.copy(
                    alpha = if (isEnable) 1f else 0.5f
                )
            } else {
                DefaultButtonColor
            }
        )
    }

    /** анимация пульсации */
    var _mScale by remember { mutableStateOf(1f) }
    val mScale by animateFloatAsState(targetValue = _mScale, label = "", animationSpec = spring(dampingRatio = 3f))
    LaunchedEffect(isPressed) {
        if (isPressed) {
            val changeList = (11..15 step 1)
                .map { it / 10f }
                .toMutableList()
                .apply {
                    addAll(this.reversed())
                }
            while (isActive) {
                changeList.forEach {
                    _mScale = it
                    delay(200)
                }
            }
        } else {
            _mScale = 1f
        }
    }

    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
            .scale(mScale)
            .clip(ButtonShape)
            .background(backgroundColor)
            .clickable(
                enabled = isEnable,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconRes?.let {
                Image(
                    modifier = Modifier
                        .size(iconSize),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(contentColor)
                )
            }
            if (iconRes != null && text.isNullOrEmpty().not()) {
                Spacer(modifier = Modifier.width(5.dp))
            }
            text?.ifEmpty { null }?.let {
                Text(
                    text = it,
                    style = MainTextStyle,
                    fontSize = textSize,
                    color = contentColor
                )
            }
        }
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
            iconSize = 32.dp,
            isPressed = true
        )
        Text(text = "нажатая")

        MIconButton(
            iconRes = null
        )
        Text(text = "нет иконки")

        MIconButton(
            text = null
        )
        Text(text = "нет текста")

        MIconButton(
            text = null,
            iconRes = null
        )
        Text(text = "пустая")
    }
}