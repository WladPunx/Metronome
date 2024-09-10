package com.wladkoshelev.metronome.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.theme.AppBarColor
import com.wladkoshelev.metronome.theme.AppBarDividerColor
import com.wladkoshelev.metronome.theme.ErrorTextColor
import com.wladkoshelev.metronome.theme.MainTextSelectionColor
import com.wladkoshelev.metronome.theme.MainTextStyle

/** кастомный АппБар с название экрана и небольшим отступом после, чтобы сохранить общий дизайн отступа для всех экранов
 *
 * название редактируемое, если {[onTextChange]} != null
 *
 * @param hint подсказка если {[title]} пустой
 * @param errorText текст ошибки. виден, если !=null */
@Composable
@Preview
fun EditableFragmentTitle(
    title: String = "title",
    hint: String = "",
    errorText: String? = null,
    onTextChange: ((String) -> Unit)? = null
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBarColor)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompositionLocalProvider(LocalTextSelectionColors provides MainTextSelectionColor) {
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onTextChange != null,
                    value = title,
                    singleLine = true,
                    onValueChange = { onTextChange?.invoke(it) },
                    textStyle = AppBarTextStyle.copy(textAlign = TextAlign.Center),
                    decorationBox = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier
                                        .alpha(if (title.isEmpty()) 1f else 0f),
                                    text = hint,
                                    style = AppBarTextStyle,
                                    color = AppBarTextStyle.color.copy(alpha = 0.6f)
                                )
                                it()
                            }
                        }
                    }
                )
            }
            AnimatedVisibility(visible = errorText != null) {
                Text(
                    text = errorText.orEmpty().lowercase(),
                    style = MainTextStyle,
                    fontSize = 14.sp,
                    color = ErrorTextColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(AppBarDividerColor)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
    }
}

private val AppBarTextStyle = MainTextStyle.copy(fontSize = 30.sp)


@Composable
@Preview
private fun Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        EditableFragmentTitle()
        Text(text = "дэфолтная")

        EditableFragmentTitle(
            title = "",
            hint = "hint"
        )
        Text(text = "виден хинт")

        EditableFragmentTitle(
            title = "text",
            errorText = "error"
        )
        Text(text = "ошибка")
    }
}