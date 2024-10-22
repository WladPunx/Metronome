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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
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
import com.wladkoshelev.metronome.destinations.SongsFragmentDestination
import com.wladkoshelev.metronome.theme.BottomControlButtonSize
import com.wladkoshelev.metronome.theme.BottomControlPadding
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.theme.EmptyListTextStyle
import com.wladkoshelev.metronome.theme.ListElementDividerPadding
import com.wladkoshelev.metronome.theme.ListElementDividerSize
import com.wladkoshelev.metronome.theme.ListElementHorizontalPadding
import com.wladkoshelev.metronome.ui.metronome.getMetronomeFragment
import com.wladkoshelev.metronome.ui.playlist.getCreateOrEditPlayListFragment
import com.wladkoshelev.metronome.ui.songs.SongsVM.VM.Event
import com.wladkoshelev.metronome.ui.songs.SongsVM.VM.Intent
import com.wladkoshelev.metronome.ui.songs.SongsVM.VM.State
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.ui.views.SongInfoView
import com.wladkoshelev.metronome.ui.views.simpleVerticalScrollbar
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance.Companion.myNavigate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.androidx.compose.koinViewModel


fun getSongsFragment(playListID: String? = null) = NavigationInstance(SongsFragmentDestination(playListID = playListID))

@RootNavGraph
@Destination
@Composable
fun SongsFragment(
    navController: NavController,
    playListID: String?
) {
    val vm = koinViewModel<SongsVM.VM> { SongsVM().params(playListID = playListID) }
    val state: State by vm.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
    val event: Flow<Event> by remember { mutableStateOf(vm.event) }
    UI(
        state = state,
        intent = intent
    )

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.NavigateToMetronome>().collect {
            navController.myNavigate(getMetronomeFragment(it.songId, it.playListID))
        }
    }

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.NavigateToEditPlayList>().collect {
            navController.myNavigate(getCreateOrEditPlayListFragment(it.playListID))
        }
    }

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.OnBack>().collect {
            navController.popBackStack()
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
            title = state.playListName ?: stringResource(R.string.all_song_title)
        )
        SongsBlock(
            modifier = Modifier.fillMaxSize(),
            songList = state.songsList,
            intent = intent,
            mode = state.mode
        )
    }
}


@Composable
@Preview
private fun SongsBlock(
    modifier: Modifier = Modifier,
    songList: List<SongData> = emptyList(),
    intent: (Intent) -> Unit = {},
    mode: State.Mode = State.Mode.ALL_SONG
) {
    val stateListState = rememberLazyListState()
    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .simpleVerticalScrollbar(stateListState),
            state = stateListState
        ) {
            itemsIndexed(items = songList, key = { index, item -> item.toString() }) { index, item ->
                Box(
                    modifier = Modifier
                        .padding(bottom = ListElementDividerPadding)
                        .fillMaxWidth()
                        .height(ListElementDividerSize)
                        .background(DividerColor)
                )
                SongInfoView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { intent(Intent.SongClick(item)) }
                        .padding(ListElementHorizontalPadding),
                    song = item,
                    number = when (mode) {
                        State.Mode.ALL_SONG -> null
                        State.Mode.PLAYLIST -> "${index + 1}"
                    }
                )
                Box(
                    modifier = Modifier
                        .padding(top = ListElementDividerPadding)
                        .fillMaxWidth()
                        .height(ListElementDividerSize)
                        .background(DividerColor)
                )
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
        if (songList.isEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = stringResource(R.string.no_song),
                style = EmptyListTextStyle
            )
        }
        MIconButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(BottomControlPadding),
            iconRes = when (mode) {
                State.Mode.ALL_SONG -> R.drawable.ic_add
                State.Mode.PLAYLIST -> R.drawable.ic_edit
            },
            onClick = {
                when (mode) {
                    State.Mode.ALL_SONG -> intent(Intent.SongClick(null))
                    State.Mode.PLAYLIST -> intent(Intent.EditPlayListClick())
                }
            },
            text = null,
            iconSize = BottomControlButtonSize
        )
    }

}
