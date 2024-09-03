package com.wladkoshelev.metronome.ui.views

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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