package com.wladkoshelev.metronome.ui.playlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.database.SongSaveStatus
import com.wladkoshelev.metronome.destinations.CreateOrEditPlayListFragmentDestination
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Event
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Intent
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.State
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MCheckbox
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.ui.views.SongInfoView
import com.wladkoshelev.metronome.ui.views.simpleVerticalScrollbar
import com.wladkoshelev.metronome.utils.NavigationInstance
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


fun getCreateOrEditPlayListFragment(playListID: String? = null) = NavigationInstance(CreateOrEditPlayListFragmentDestination(playListId = playListID))

@RootNavGraph
@Destination
@Composable
fun CreateOrEditPlayListFragment(
    navController: NavController,
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
        EditableFragmentTitle(
            title = state.name,
            onTextChange = { intent(Intent.SetName(it)) },
            hint = stringResource(R.string.play_list_name_hint),
            errorText = when (state.saveStatus) {
                SongSaveStatus.SUCCESS -> null
                SongSaveStatus.EMPTY_NAME -> stringResource(R.string.save_status_empty_name)
                SongSaveStatus.NAME_EXIST -> stringResource(R.string.save_status_name_exist)
            }
        )
        ControlBlock(
            modifier = Modifier.fillMaxWidth(),
            isCanDelete = state.isCanDelete,
            isCanSave = state.isCanSave,
            intent = intent
        )
        Spacer(modifier = Modifier.height(10.dp))

        SongsBlock(
            modifier = Modifier
                .weight(1f),
            selectSongs = state.selectSong,
            unSelectSongs = state.unSelectSong,
            intent = intent
        )

    }
}


@Composable
@Preview
private fun ControlBlock(
    modifier: Modifier = Modifier,
    isCanSave: Boolean = true,
    isCanDelete: Boolean = true,
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_save,
                isEnable = isCanSave,
                onClick = { intent(Intent.SavePlayList()) }
            )
            Spacer(modifier = Modifier.width(50.dp))
            MIconButton(
                iconRes = R.drawable.ic_burger,
                isEnable = isCanDelete,
                onClick = { intent(Intent.DeletePlayList()) }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


/** Блок для отображения списка песен
 *
 * выбранные песни отображаются первыми в списке и их можно менять местами друг с другом */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
private fun SongsBlock(
    modifier: Modifier = Modifier,
    selectSongs: List<SongData> = emptyList(),
    unSelectSongs: List<SongData> = emptyList(),
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = modifier
    ) {
        val lazyListState = rememberLazyListState()
        val state = rememberReorderableLazyListState(lazyListState = lazyListState, onMove = { from, to ->
            intent(Intent.OnMoveSelectSong(from = from.index, to = to.index))
        })
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(lazyListState),
            state = lazyListState
        ) {
            items(selectSongs, { it.toString() }) { item ->
                ReorderableItem(state, key = item.toString()) { isDragging ->
                    val backgroundAnimate by animateColorAsState(
                        targetValue = if (isDragging) DefaultButtonColor.copy(alpha = 0.5f) else Color.Transparent, label = "",
                        animationSpec = spring(dampingRatio = 3f)
                    )
                    SongForPlayListUI(
                        modifier = Modifier
                            .animateItemPlacement()
                            .fillMaxWidth()
                            .longPressDraggableHandle()
                            .background(backgroundAnimate),
                        item = item,
                        isCheck = true,
                        onCheckChange = { intent(Intent.CheckUnCheckSong(item)) }
                    )
                }
            }
            items(unSelectSongs, { it.toString() }) {
                SongForPlayListUI(
                    modifier = Modifier
                        .animateItemPlacement()
                        .fillMaxWidth(),
                    item = it,
                    isCheck = false,
                    onCheckChange = { intent(Intent.CheckUnCheckSong(it)) }
                )
            }
        }
    }

}


/** Элемент песни в Списке {[SongsBlock]} */
@Composable
@Preview
private fun SongForPlayListUI(
    modifier: Modifier = Modifier,
    item: SongData = SongData.mock(),
    isCheck: Boolean = true,
    onCheckChange: () -> Unit = {}
) {
    val dividerSize by remember { mutableStateOf(0.5.dp) }
    val spacerSize by remember { mutableStateOf(10.dp) }
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
            .padding(horizontal = 10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(bottom = spacerSize)
                    .fillMaxWidth()
                    .height(dividerSize)
                    .background(DividerColor)
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MCheckbox(
                    isCheck = isCheck,
                    onCheck = { onCheckChange() }
                )
                SongInfoView(
                    modifier = Modifier.weight(1f),
                    song = item
                )
                if (isCheck) {
                    Image(
                        painter = painterResource(R.drawable.ic_burger),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(DefaultButtonColor)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(top = spacerSize)
                    .fillMaxWidth()
                    .height(dividerSize)
                    .background(DividerColor)
            )
        }

    }
}
