package com.wladkoshelev.metronome.ui.metronome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.destinations.MetronomeFragmentDestination
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.Event
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.Intent
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.State
import com.wladkoshelev.metronome.utils.NavigationInstance
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel


class MetronomeFragment {
    companion object {

        fun get(songId: String? = null) = NavigationInstance(MetronomeFragmentDestination(songId))

        @RootNavGraph
        @Destination
        @Composable
        fun MetronomeFragment(
            songsId: String?
        ) {
            val vm = koinViewModel<MetronomeVM.VM> { MetronomeVM().params(songsId) }
            val state: State by vm.state.collectAsStateWithLifecycle()
            val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
            val event: Flow<Event> by remember { mutableStateOf(vm.event) }
            MetronomeFragment().UI(
                state = state,
                intent = intent
            )

            MetronomeFragment().InputAlertDialog(
                title = "настройка бмп",
                isShow = state.isShowEditBmp,
                onDismiss = { intent(Intent.IsShowEditBmp(false)) },
                defaultValue = state.metronomeState.bmp,
                onSuccess = { intent(Intent.SetSpeed(it)) }
            )

            MetronomeFragment().InputAlertDialog(
                title = "настройка размера такса",
                isShow = state.isShowEditTactSize,
                onDismiss = { intent(Intent.IsShowEditTactSize(false)) },
                defaultValue = state.metronomeState.tactSize,
                onSuccess = { intent(Intent.SetSTactSize(it)) }
            )

        }
    }

    @Composable
    @Preview
    private fun UI(
        state: State = State(songId = "songID"),
        intent: (Intent) -> Unit = {}
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                modifier = Modifier
                    .clickable { intent(Intent.IsShowEditBmp(true)) },
                text = state.metronomeState.bmp.toString()
            )

            Text(
                modifier = Modifier
                    .clickable { intent(Intent.IsShowEditTactSize(true)) },
                text = state.metronomeState.tactSize.toString()
            )


            Button(onClick = { intent(Intent.Play()) }) {
                Text(text = "игрта")
            }
            Button(onClick = { intent(Intent.Stop()) }) {
                Text(text = "стоп")
            }
            TextField(
                value = state.songName,
                onValueChange = {
                    intent(Intent.SetName(it))
                }
            )
            if (state.isShowSave) {
                Button(onClick = { intent(Intent.SaveSong()) }) {
                    Text(text = "save")
                }
            }
            if (state.isShowDelete) {
                Button(onClick = { intent(Intent.DeleteSong()) }) {
                    Text(text = "delete")
                }
            }

        }
    }


    /** АлертДиалог для ввода размера такта и скорости
     *
     * работает с Int'овым значениями */
    @Composable
    @Preview
    private fun InputAlertDialog(
        isShow: Boolean = true,
        onDismiss: () -> Unit = {},
        onSuccess: (Int) -> Unit = {},
        title: String = "title",
        defaultValue: Int = 120,
    ) {
        if (isShow) {
            var text by remember(defaultValue) { mutableStateOf(defaultValue.toString()) }

            Dialog(onDismissRequest = { onDismiss() }) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                ) {
                    Text(text = title)
                    TextField(
                        value = text,
                        onValueChange = {
                            if (it.isDigitsOnly()) {
                                text = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Button(onClick = {
                        onSuccess(
                            runCatching { text.toInt() }.getOrNull() ?: 0
                        )
                        onDismiss()
                    }) {
                        Text(text = "готово")
                    }
                    Button(onClick = { onDismiss() }) {
                        Text(text = "закрыть")
                    }
                }
            }
        }

    }

}
