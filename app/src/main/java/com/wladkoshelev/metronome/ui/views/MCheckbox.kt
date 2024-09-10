package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.wladkoshelev.metronome.theme.DefaultButtonColor

/** стилизованный ЧекБокс для всего приложения */
@Composable
@Preview
fun MCheckbox(
    modifier: Modifier = Modifier,
    isCheck: Boolean = false,
    onCheck: () -> Unit = {}
) {
    Checkbox(
        modifier = modifier,
        checked = isCheck,
        onCheckedChange = { onCheck() },
        colors = CheckboxDefaults.colors(
            checkedColor = DefaultButtonColor,
            uncheckedColor = DefaultButtonColor,
            checkmarkColor = Color.White
        )
    )
}

@Composable
@Preview
private fun Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        MCheckbox()
        Text(text = "дэфолтная")

        MCheckbox(isCheck = true)
        Text(text = "включенная")

        MCheckbox(isCheck = true, modifier = Modifier.fillMaxWidth())
        Text(text = "на всю ширину")
    }
}