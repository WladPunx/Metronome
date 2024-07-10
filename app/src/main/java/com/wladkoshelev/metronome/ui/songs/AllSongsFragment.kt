package com.wladkoshelev.metronome.ui.songs

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.AllSongsFragmentDestination
import com.wladkoshelev.metronome.ui.metronome.getMetronomeFragment
import com.wladkoshelev.metronome.utils.NavigationInstance
import com.wladkoshelev.metronome.utils.NavigationInstance.Companion.myNavigate
import org.koin.androidx.compose.koinViewModel


fun getAllSongsFragment() = NavigationInstance(AllSongsFragmentDestination())

@RootNavGraph
@Destination
@Composable
fun AllSongsFragment(
    navController: NavController
) {
    val vm = koinViewModel<AllSongsVM.VM> { AllSongsVM().params() }
    val state by vm.state.collectAsStateWithLifecycle()
    val intent = remember { vm::sendIntent }
    UI(
        state = state,
        intent = intent
    )

    LaunchedEffect(Unit) {
        vm.event.collect {
            when (it) {
                is AllSongsVM.VM.Event.NavigateToMetronomeWithSong -> navController.myNavigate(getMetronomeFragment(it.songId))
            }
        }
    }
}

@Composable
@Preview
private fun UI(
    state: AllSongsVM.VM.State = AllSongsVM.VM.State(),
    intent: (AllSongsVM.VM.Intent) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(state.songsList) {
                SongItem(
                    modifier = Modifier
                        .clickable { intent(AllSongsVM.VM.Intent.SongClick(it)) },
                    song = it
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
private fun SongItem(
    modifier: Modifier = Modifier,
    song: SongData = SongData(
        id = "id",
        name = "name",
        speed = 45,
        tactSize = 23
    )
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Column {
            Text(text = "id - ${song.id}")
            Text(text = "name - ${song.name}")
            Text(text = "speed - ${song.speed}")
            Text(text = "tactSize - ${song.tactSize}")
        }
    }
}