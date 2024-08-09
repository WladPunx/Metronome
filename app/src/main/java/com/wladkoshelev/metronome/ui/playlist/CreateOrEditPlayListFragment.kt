package com.wladkoshelev.metronome.ui.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.CreateOrEditPlayListFragmentDestination
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Event
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Intent
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.State
import com.wladkoshelev.metronome.utils.NavigationInstance
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel


fun getCreateOrEditPlayListFragment(playListID: String? = null) = NavigationInstance(CreateOrEditPlayListFragmentDestination(playListID))

@RootNavGraph
@Destination
@Composable
fun CreateOrEditPlayListFragment(
    playListId: String?
) {
    val vm = koinViewModel<CreateOrEditPlayListVM.VM> { CreateOrEditPlayListVM().params(playListId) }
    val state: State by vm.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
    val event: Flow<Event> by remember { mutableStateOf(vm.event) }
    UI(
        state = state,
        intent = intent
    )
}


@Composable
@Preview
private fun UI(
    state: State = State(playListId = "gg"),
    intent: (Intent) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TextField(
            value = state.name,
            onValueChange = {
                intent(Intent.SetName(it))
            }
        )
        Button(onClick = { intent(Intent.SavePlayList()) }) {
            Text(text = "save")
        }
        LazyColumn {
            items(state.songsWithCheck) {
                SongForPlayListUI(item = it, onCheckChange = { intent(Intent.CheckUnCheckSong(it)) })
            }
        }
    }
}

@Composable
private fun SongForPlayListUI(
    modifier: Modifier = Modifier,
    item: Pair<SongData, Boolean>,
    onCheckChange: () -> Unit
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row {
            Checkbox(checked = item.second, onCheckedChange = { onCheckChange() })
            Text(text = item.first.name)
        }
    }
}


