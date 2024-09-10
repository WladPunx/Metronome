package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.theme.ButtonShape
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.mainFont

/** стилизованная кнопка для всего приложения */
@Composable
@Preview
fun MButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String = "text",
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isPressed: Boolean = interactionSource.collectIsPressedAsState().value
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPressed) ButtonTextColor else DefaultButtonColor
        ),
        border = BorderStroke(
            width = if (isPressed) 1.dp else 0.dp,
            color = DefaultButtonColor
        ),
        shape = ButtonShape
    ) {
        Text(
            text = text.uppercase(),
            color = if (isPressed) DefaultButtonColor else ButtonTextColor,
            fontFamily = mainFont,
            fontSize = 16.sp
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
        MButton()
        Text(text = "базовая")

        MButton(isPressed = true)
        Text(text = "нажатая")

        MButton(text = "mghsdsdsdfssdfg")
        Text(text = "свой текст")

        MButton(
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "на всю ширину")
    }
}