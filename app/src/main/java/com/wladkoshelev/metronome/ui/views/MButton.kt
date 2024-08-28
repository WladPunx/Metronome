package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.mainFont

/** стилизованная кнопка для всего приложения */
@Composable
@Preview
fun MButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String = "text"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
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
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = if (isPressed) DefaultButtonColor else ButtonTextColor,
            fontFamily = mainFont,
            fontSize = 16.sp
        )
    }
}