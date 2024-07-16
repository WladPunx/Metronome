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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.CreateOrEditPlayListFragmentDestination
import com.wladkoshelev.metronome.utils.NavigationInstance
import org.koin.androidx.compose.koinViewModel


class CreateOrEditPlayListFragment {
    companion object {

        fun get(playListID: String? = null) = NavigationInstance(CreateOrEditPlayListFragmentDestination(playListID))

        @RootNavGraph
        @Destination
        @Composable
        fun CreateOrEditPlayListFragment(
            playListId: String?
        ) {
            val vm = koinViewModel<CreateOrEditPlayListVM.VM> { CreateOrEditPlayListVM().params(playListId) }
            val state by vm.state.collectAsStateWithLifecycle()
            val intent = remember { vm::sendIntent }
            CreateOrEditPlayListFragment().UI(
                state = state,
                intent = intent
            )
        }
    }


    @Composable
    @Preview
    private fun UI(
        state: CreateOrEditPlayListVM.VM.State = CreateOrEditPlayListVM.VM.State(playListId = "gg"),
        intent: (CreateOrEditPlayListVM.VM.Intent) -> Unit = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TextField(
                value = state.name,
                onValueChange = {
                    intent(CreateOrEditPlayListVM.VM.Intent.SetName(it))
                }
            )
            Button(onClick = { intent(CreateOrEditPlayListVM.VM.Intent.SavePlayList()) }) {
                Text(text = "save")
            }
            LazyColumn {
                items(state.songsWithCheck) {
                    SongForPlayListUI(item = it, onCheckChange = { intent(CreateOrEditPlayListVM.VM.Intent.CheckUnCheckSong(it)) })
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

}
