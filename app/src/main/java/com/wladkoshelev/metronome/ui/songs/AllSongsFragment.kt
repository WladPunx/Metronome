package com.wladkoshelev.metronome.ui.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.AllSongsFragmentDestination
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.ui.metronome.getMetronomeFragment
import com.wladkoshelev.metronome.ui.songs.AllSongsVM.VM.Event
import com.wladkoshelev.metronome.ui.songs.AllSongsVM.VM.Intent
import com.wladkoshelev.metronome.ui.songs.AllSongsVM.VM.State
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MButton
import com.wladkoshelev.metronome.ui.views.SongInfoView
import com.wladkoshelev.metronome.ui.views.simpleVerticalScrollbar
import com.wladkoshelev.metronome.utils.NavigationInstance
import com.wladkoshelev.metronome.utils.NavigationInstance.Companion.myNavigate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.androidx.compose.koinViewModel


fun getAllSongsFragment() = NavigationInstance(AllSongsFragmentDestination())

@RootNavGraph
@Destination
@Composable
fun AllSongsFragment(
    navController: NavController
) {
    val vm = koinViewModel<AllSongsVM.VM> { AllSongsVM().params() }
    val state: State by vm.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
    val event: Flow<Event> by remember { mutableStateOf(vm.event) }
    UI(
        state = state,
        intent = intent
    )

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.NavigateToMetronome>().collect {
            navController.myNavigate(getMetronomeFragment(it.songId))
        }
    }
}


@Composable
@Preview
private fun UI(
    state: State = State(),
    intent: (Intent) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EditableFragmentTitle(
            title = stringResource(R.string.all_song_title)
        )
        SongsBlock(
            modifier = Modifier.weight(1f),
            songList = state.songsList,
            intent = intent
        )
    }
}


@Composable
@Preview
private fun SongsBlock(
    modifier: Modifier = Modifier,
    songList: List<SongData> = emptyList(),
    intent: (Intent) -> Unit = {}
) {
    val stateListState = rememberLazyListState()
    val dividerSize by remember { mutableStateOf((1.0 / 2.0).dp) }
    val spacerSize by remember { mutableStateOf(10.dp) }
    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .simpleVerticalScrollbar(stateListState),
            state = stateListState
        ) {
            items(songList) { item ->
                Box(
                    modifier = Modifier
                        .padding(bottom = spacerSize)
                        .fillMaxWidth()
                        .height(dividerSize)
                        .background(DividerColor)
                )
                SongInfoView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { intent(Intent.SongClick(item)) }
                        .padding(start = 30.dp),
                    song = item
                )
                Box(
                    modifier = Modifier
                        .padding(top = spacerSize)
                        .fillMaxWidth()
                        .height(dividerSize)
                        .background(DividerColor)
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
        MButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp),
            text = stringResource(R.string.add_new_song),
            onClick = { intent(Intent.SongClick(null)) }
        )
    }

}
