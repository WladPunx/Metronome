package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wladkoshelev.metronome.theme.ModalWindowBackgroundColor

/** общий БоттомШит для приложения
 *
 * @param skipPartiallyExpanded пропускать ли промежуточное состояние между "скрытым" и "полностью открыт".
 * true - пропускать */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MBottomSheet(
    modifier: Modifier = Modifier,
    isShow: Boolean = true,
    onDismiss: () -> Unit = {},
    skipPartiallyExpanded: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    if (isShow) {
        ModalBottomSheet(
            modifier = modifier,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
            containerColor = ModalWindowBackgroundColor,
            onDismissRequest = { onDismiss() },
        ) {
            content()
        }
    }
}
