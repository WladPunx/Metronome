package com.wladkoshelev.metronome.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.wladkoshelev.metronome.database.PlayListData
import com.wladkoshelev.metronome.destinations.PlayListsFragmentDestination
import com.wladkoshelev.metronome.theme.BottomControlButtonSize
import com.wladkoshelev.metronome.theme.BottomControlPadding
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.theme.EmptyListTextStyle
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.theme.SecondTextStyle
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.Event
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.Intent
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.State
import com.wladkoshelev.metronome.ui.songs.getSongsFragment
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.ui.views.simpleVerticalScrollbar
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance.Companion.myNavigate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.androidx.compose.koinViewModel


fun getPlayListsFragment() = NavigationInstance(PlayListsFragmentDestination())

@RootNavGraph
@Destination
@Composable
fun PlayListsFragment(
    navController: NavController
) {
    val vm = koinViewModel<PlayListsVM.VM> { PlayListsVM().params() }
    val state: State by vm.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
    val event: Flow<Event> by remember { mutableStateOf(vm.event) }
    UI(
        state = state,
        intent = intent
    )
    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.CreateNewPlatList>().collect {
            navController.myNavigate(getCreateOrEditPlayListFragment())
        }
    }

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.OpenPlayList>().collect {
            navController.myNavigate(getSongsFragment(it.playListID))
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
            title = stringResource(R.string.playlist_fragment_title)
        )
        val listState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .simpleVerticalScrollbar(listState)
            ) {
                itemsIndexed(items = state.allPlayLists, key = { index, item -> item.toString() }) { index, item ->
                    Divider(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                    )
                    PlayListUI(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clickable {
                                intent(Intent.OpenPlayList(item.id))
                            },
                        playList = item
                    )
                    Divider(
                        modifier = Modifier
                            .padding(top = 10.dp)
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
            if (state.allPlayLists.isEmpty()) {
                Text(text = stringResource(R.string.no_playlist), Modifier.align(Alignment.Center), style = EmptyListTextStyle)
            }
            MIconButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(BottomControlPadding),
                text = null,
                iconRes = R.drawable.ic_add,
                iconSize = BottomControlButtonSize,
                onClick = { intent(Intent.CreateNewPlayList()) }
            )
        }
    }

}


@Composable
@Preview
private fun PlayListUI(
    modifier: Modifier = Modifier,
    playList: PlayListData = PlayListData(
        id = "id",
        name = "name",
        songsList = emptyList()
    )
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Column {
            Text(text = playList.name, style = MainTextStyle)
            Spacer(modifier = Modifier.height(5.dp))
            var text = ""
            playList.songsList.forEachIndexed { index, songData ->
                text += "${index + 1}. ${songData.name}; "
            }
            Text(
                text = text,
                style = SecondTextStyle
            )
        }
    }
}


@Composable
@Preview
private fun Divider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(DividerColor)
    )
}


