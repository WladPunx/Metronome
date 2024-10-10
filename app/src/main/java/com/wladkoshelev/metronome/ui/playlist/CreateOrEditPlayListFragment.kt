package com.wladkoshelev.metronome.ui.playlist

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.CreateOrEditPlayListFragmentDestination
import com.wladkoshelev.metronome.theme.BottomControlButtonSize
import com.wladkoshelev.metronome.theme.BottomControlPadding
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.theme.EmptyListTextStyle
import com.wladkoshelev.metronome.theme.ListElementDividerPadding
import com.wladkoshelev.metronome.theme.ListElementDividerSize
import com.wladkoshelev.metronome.theme.ListElementHorizontalPadding
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Event
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.Intent
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM.VM.State
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MAlertButton
import com.wladkoshelev.metronome.ui.views.MAlertDialog
import com.wladkoshelev.metronome.ui.views.MBottomSheet
import com.wladkoshelev.metronome.ui.views.MCheckbox
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.ui.views.SongInfoView
import com.wladkoshelev.metronome.ui.views.parseToTextToEditableFragment
import com.wladkoshelev.metronome.ui.views.simpleVerticalScrollbar
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
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
    BackHandler {
        intent(Intent.OnBackPressed())
    }
    /** модалка по добавлению песен */
    MBottomSheet(
        modifier = Modifier,
        isShow = state.isShowAllSongs,
        onDismiss = { intent(Intent.SetIsShowAllSong(false)) }
    ) {
        AddSongsBlock(
            intent = intent,
            selectSongs = state.selectSong,
            unSelectSongs = state.unSelectSong
        )
    }
    /** диалог удаления плейлиста */
    MAlertDialog(
        isShow = state.isShowDeleteAlertDialog,
        onDismiss = { intent(Intent.IsShowDeleteAlert(false)) },
        title = stringResource(R.string.alert_delete_playlist_title),
        buttons = listOf(
            MAlertButton(stringResource(R.string.alert_delete_playlist_yes)) { intent(Intent.DeletePlayList()) },
            MAlertButton(stringResource(R.string.alert_delete_playlist_no)) { intent(Intent.IsShowDeleteAlert(false)) }
        )
    )
    /** диалог для выхода с экрана, когда можно сохранить */
    MAlertDialog(
        isShow = state.isShowExitDialog,
        title = stringResource(R.string.exit_without_save_title),
        text = stringResource(R.string.exit_without_save_message),
        buttons = listOf(
            MAlertButton(stringResource(R.string.exit_without_save_save_and_exit)) { intent(Intent.SaveAndExit()) },
            MAlertButton(stringResource(R.string.exit_without_save_exit_without_save)) { intent(Intent.ExitWithoutSave()) },
        )
    )

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.OnBack>().collect {
            navController.popBackStack()
        }
    }
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
            errorText = state.saveStatus.parseToTextToEditableFragment()
        )
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            MoveSongsBlock(
                modifier = Modifier.fillMaxSize(),
                selectSongs = state.selectSong,
                intent = intent
            )
            ControlBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BottomControlPadding)
                    .align(Alignment.BottomCenter),
                isCanDelete = state.isCanDelete,
                isCanSave = state.isCanSave,
                intent = intent
            )
        }
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
    val iconSize = BottomControlButtonSize
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_save,
                text = null,
                isEnable = isCanSave,
                iconSize = iconSize,
                onClick = { intent(Intent.SavePlayList()) }
            )
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_add,
                text = null,
                iconSize = iconSize,
                onClick = { intent(Intent.SetIsShowAllSong(true)) }
            )
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_delete,
                text = null,
                iconSize = iconSize,
                isEnable = isCanDelete,
                onClick = { intent(Intent.IsShowDeleteAlert(true)) }
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
private fun MoveSongsBlock(
    modifier: Modifier = Modifier,
    selectSongs: List<SongData> = emptyList(),
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = modifier
    ) {
        val lazyListState = rememberLazyListState()
        val state = rememberReorderableLazyListState(lazyListState = lazyListState, onMove = { from, to ->
            intent(Intent.OnMoveSelectSong(from = from.index, to = to.index))
        })
        if (selectSongs.isEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                text = stringResource(R.string.playlist_no_song_in_playlist),
                style = EmptyListTextStyle,
            )
            return
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .simpleVerticalScrollbar(lazyListState),
            state = lazyListState
        ) {
            itemsIndexed(items = selectSongs, key = { index, item -> item.toString() }) { index, item ->
                ReorderableItem(state, key = item.toString()) { isDragging ->
                    val backgroundAnimate by animateColorAsState(
                        targetValue = if (isDragging) DefaultButtonColor.copy(alpha = 0.5f) else Color.Transparent, label = "",
                        animationSpec = spring(dampingRatio = 3f)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .longPressDraggableHandle()
                            .background(backgroundAnimate)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = ListElementDividerPadding)
                                .fillMaxWidth()
                                .height(ListElementDividerSize)
                                .background(DividerColor)
                        )
                        Row(
                            modifier = Modifier.padding(ListElementHorizontalPadding),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SongInfoView(
                                modifier = Modifier.weight(1f),
                                song = item,
                                number = "${index + 1}"
                            )
                            Image(
                                painter = painterResource(R.drawable.ic_burger),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(DefaultButtonColor)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = ListElementDividerPadding)
                                .fillMaxWidth()
                                .height(ListElementDividerSize)
                                .background(DividerColor)
                        )
                    }

                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

}


/** блок для Добавления/Удаления песен из плейлиста */
@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
private fun AddSongsBlock(
    intent: (Intent) -> Unit = {},
    selectSongs: List<SongData> = emptyList(),
    unSelectSongs: List<SongData> = emptyList(),
) {
    val allSongs by remember(selectSongs, unSelectSongs) {
        mutableStateOf(
            selectSongs.toMutableList().apply {
                addAll(unSelectSongs)
            }.toList()
        )
    }
    Column {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.playlist_add_song_description),
            style = MainTextStyle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(15.dp))
        if (allSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = stringResource(R.string.no_song),
                    style = EmptyListTextStyle
                )
            }
            return
        }
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .simpleVerticalScrollbar(lazyListState),
            state = lazyListState
        ) {
            item { Spacer(modifier = Modifier.height(1.dp)) }
            items(allSongs, { it.toString() }) { item ->
                Column(
                    modifier = Modifier
                        .animateItemPlacement()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = ListElementDividerPadding)
                            .fillMaxWidth()
                            .height(ListElementDividerSize)
                            .background(DividerColor)
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MCheckbox(
                            isCheck = selectSongs.contains(item),
                            onCheck = { intent(Intent.CheckUnCheckSong(item)) }
                        )
                        SongInfoView(
                            modifier = Modifier.weight(1f),
                            song = item
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = ListElementDividerPadding)
                            .fillMaxWidth()
                            .height(ListElementDividerSize)
                            .background(DividerColor)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }


}


