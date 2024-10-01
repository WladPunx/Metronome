package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wladkoshelev.metronome.theme.AlertDialogTextStyle
import com.wladkoshelev.metronome.theme.AlertDialogTitleStyle
import com.wladkoshelev.metronome.theme.ButtonShape
import com.wladkoshelev.metronome.theme.ModalWindowBackgroundColor

/** единый АлертДиалог для всего приложения
 *
 *  {[text]} {[title]} показываются, только если они не null и не пустые
 *
 *  {[buttons]} - это список кнопок, который находятся во {[FlowRow]}.
 *  размер текста кнопок автоматически уменьшается, если не влезается в 1 строку и применяется ко всем кнопкам.
 *
 * @param title шапка алерта
 * @param text месседж алерта
 * @param [buttons] список кнопок. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun MAlertDialog(
    isShow: Boolean = true,
    onDismiss: () -> Unit = {},
    title: String? = null,
    text: String? = null,
    buttons: List<MAlertButton> = emptyList()
) {
    if (isShow) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                modifier = Modifier
                    .clip(ButtonShape)
                    .background(ModalWindowBackgroundColor)
                    .padding(20.dp)
            ) {
                title?.ifEmpty { null }?.let {
                    Text(text = it, style = AlertDialogTitleStyle)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                text?.ifEmpty { null }?.let {
                    Text(text = it, style = AlertDialogTextStyle)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(15.dp))
                var textSizePercent by remember { mutableStateOf(1f) }
                FlowRow {
                    buttons.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp)
                                .padding(end = 5.dp, bottom = 5.dp)
                                .clip(ButtonShape)
                                .border(1.dp, AlertDialogTitleStyle.color, ButtonShape)
                                .clickable {
                                    it.onClick()
                                }
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = it.text,
                                style = AlertDialogTitleStyle,
                                fontSize = AlertDialogTitleStyle.fontSize * textSizePercent,
                                maxLines = 1,
                                onTextLayout = {
                                    if (it.hasVisualOverflow) {
                                        textSizePercent = (textSizePercent - 0.1f).coerceAtLeast(0.1f)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** модель для кнопок для {[MAlertDialog]}
 *
 * @param text текст кнопки
 * @param onClick код для клика */
data class MAlertButton(
    val text: String,
    val onClick: () -> Unit
)
