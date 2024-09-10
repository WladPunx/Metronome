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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.database.PlayListData
import com.wladkoshelev.metronome.destinations.PlayListsFragmentDestination
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.Event
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.Intent
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM.VM.State
import com.wladkoshelev.metronome.utils.NavigationInstance
import com.wladkoshelev.metronome.utils.NavigationInstance.Companion.myNavigate
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
        event.filterIsInstance<Event.EditPlayList>().collect {
            navController.myNavigate(getCreateOrEditPlayListFragment(it.playListID))
        }
    }
}


@Composable
@Preview
private fun UI(
    state: State = State(),
    intent: (Intent) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { intent(Intent.CreateNewPlayList()) }) {
            Text(text = "создать новый плейлист")
        }
        LazyColumn {
            items(state.allPlayLists) {
                PlayListUI(
                    modifier = Modifier.clickable {
                        intent(Intent.EditPlayList(it.id))
                    },
                    playList = it
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
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
            Text(text = "id - ${playList.id}")
            Text(text = "name - ${playList.name}")
            playList.songsList.map { it.name }.forEach {
                Text(text = "song - ${it}")
            }
        }
    }
}


