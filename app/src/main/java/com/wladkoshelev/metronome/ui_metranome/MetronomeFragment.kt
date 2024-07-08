package com.wladkoshelev.metronome.ui_metranome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.destinations.MetronomeFragmentDestination
import com.wladkoshelev.metronome.utils.NavigationInstance
import org.koin.androidx.compose.koinViewModel

@RootNavGraph
@Destination
@Composable
fun MetronomeFragment(
    navController: NavController
) {
    val vm = koinViewModel<MetronomeVM.VM> { MetronomeVM().params() }
    val state by vm.state.collectAsStateWithLifecycle()
    val intent = remember { vm::sendIntent }
    UI(
        state = state,
        intent = intent
    )
}

fun getMetronomeFragment() = NavigationInstance(MetronomeFragmentDestination())


@Composable
@Preview
private fun UI(
    state: MetronomeVM.VM.State = MetronomeVM.VM.State(),
    intent: (MetronomeVM.VM.Intent) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        @Composable
        fun Modifier.setFocusListener(): Modifier {
            val focusRequester = remember { FocusRequester() }
            return this
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        intent(MetronomeVM.VM.Intent.NoFocus())
                    }
                }
        }

        TextField(
            modifier = Modifier.setFocusListener(),
            value = state.speed?.toString() ?: "",
            onValueChange = {
                intent(MetronomeVM.VM.Intent.SetSpeed(it.toIntOrNull()))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            modifier = Modifier.setFocusListener(),
            value = state.tactSize?.toString() ?: "",
            onValueChange = {
                intent(MetronomeVM.VM.Intent.SetSTactSize(it.toIntOrNull()))

            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = { intent(MetronomeVM.VM.Intent.Play()) }) {
            Text(text = "игрта")
        }
    }
}
