package com.wladkoshelev.metronome.ui.metronome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.destinations.MetronomeFragmentDestination
import com.wladkoshelev.metronome.theme.AlertDialogInnerPadding
import com.wladkoshelev.metronome.theme.AlertDialogShape
import com.wladkoshelev.metronome.theme.AppBarDividerColor
import com.wladkoshelev.metronome.theme.BottomControlButtonSize
import com.wladkoshelev.metronome.theme.BottomControlPadding
import com.wladkoshelev.metronome.theme.ButtonShape
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DefaultButtonColor
import com.wladkoshelev.metronome.theme.MainTextSelectionColor
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.theme.ModalWindowBackgroundColor
import com.wladkoshelev.metronome.theme.SecondTextStyle
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.Event
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.Intent
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM.VM.State
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MAlertButton
import com.wladkoshelev.metronome.ui.views.MAlertButtonsView
import com.wladkoshelev.metronome.ui.views.MAlertDialog
import com.wladkoshelev.metronome.ui.views.MBottomSheet
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.ui.views.parseToTextToEditableFragment
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import org.koin.androidx.compose.koinViewModel


fun getMetronomeFragment(songId: String? = null, playListID: String? = null) =
    NavigationInstance(MetronomeFragmentDestination(songsId = songId, playListID = playListID))

@RootNavGraph
@Destination
@Composable
fun MetronomeFragment(
    navController: NavController,
    songsId: String?,
    playListID: String?
) {
    val vm = koinViewModel<MetronomeVM.VM> { MetronomeVM().params(songId = songsId, playListID = playListID) }
    val state: State by vm.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(vm::sendIntent) }
    val event: Flow<Event> by remember { mutableStateOf(vm.event) }
    UI(
        state = state,
        intent = intent
    )

    BackHandler {
        intent(Intent.OnBackClick())
    }

    LaunchedEffect(Unit) {
        event.filterIsInstance<Event.OnBack>().collect {
            navController.popBackStack()
        }
    }

    /** Алерт для ввода Скорости */
    InputAlertDialog(
        title = stringResource(R.string.metronome_bmp_title),
        isShow = state.isShowEditBmp,
        onDismiss = { intent(Intent.IsShowEditBmp(false)) },
        defaultValue = state.metronomeState.bmp,
        onSuccess = { intent(Intent.SetSpeed(it)) }
    )

    /** Алерт для редактирования Размера Такта */
    InputAlertDialog(
        title = stringResource(R.string.metronome_tact_size_title),
        isShow = state.isShowEditTactSize,
        onDismiss = { intent(Intent.IsShowEditTactSize(false)) },
        defaultValue = state.metronomeState.tactSize,
        onSuccess = { intent(Intent.SetSTactSize(it)) }
    )

    /** Алерт для выхода без сохранения */
    MAlertDialog(
        isShow = state.isShowExitWithoutSaveAlert.isShow,
        title = stringResource(R.string.exit_without_save_title),
        text = stringResource(R.string.exit_without_save_message),
        buttons = listOf(
            MAlertButton(stringResource(R.string.exit_without_save_save_and_exit)) { state.isShowExitWithoutSaveAlert.onSuccess() },
            MAlertButton(stringResource(R.string.exit_without_save_exit_without_save)) { state.isShowExitWithoutSaveAlert.onCancelClick() },
        )
    )

    /** Алерт удаления песни */
    MAlertDialog(
        isShow = state.isShowDeleteAlert,
        onDismiss = { intent(Intent.IsShowDeleteAlert(false)) },
        title = stringResource(R.string.metronome_delete_song),
        buttons = listOf(
            MAlertButton(stringResource(R.string.alert_delete_playlist_yes)) { intent(Intent.DeleteSong()) },
            MAlertButton(stringResource(R.string.alert_delete_playlist_no)) { intent(Intent.IsShowDeleteAlert(false)) }
        )
    )
    /** окно настроек звука */
    SoundSettingsBottomSheet(
        isShow = state.isShowSoundSettings,
        soundList = state.soundNameList,
        mainSound = state.metronomeState.mainSoundName,
        secondSound = state.metronomeState.secondSoundName,
        intent = intent
    )

}

@Composable
@Preview
private fun UI(
    state: State = State(songId = "songID", soundNameList = emptyList()),
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            EditableFragmentTitle(
                title = state.songName,
                onTextChange = { intent(Intent.SetName(it)) },
                hint = stringResource(R.string.metronome_song_name_hint),
                errorText = state.saveStatus.parseToTextToEditableFragment()
            )
            Spacer(modifier = Modifier.weight(1f))
            TactSizeBlock(
                modifier = Modifier.fillMaxWidth(),
                tactSize = state.metronomeState.tactSize,
                intent = intent
            )
            Spacer(modifier = Modifier.weight(1f))
            BmpBlock(
                modifier = Modifier.fillMaxWidth(),
                bmp = state.metronomeState.bmp,
                intent = intent
            )
            Spacer(modifier = Modifier.weight(1f))
            PlayControlBlock(
                modifier = Modifier.fillMaxWidth(),
                isPlay = state.metronomeState.isPlay,
                intent = intent,
                previousSong = state.previousSong,
                nextSong = state.nextSong
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(100.dp))
        }

        BottomControlPanel(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            intent = intent,
            isCanSave = state.isCanSave,
            isCanDelete = state.isCanDelete
        )
    }


}


/** АлертДиалог для ввода размера такта и скорости
 *
 * работает с Int'овым значениями */
@Composable
@Preview
private fun InputAlertDialog(
    isShow: Boolean = true,
    onDismiss: () -> Unit = {},
    onSuccess: (Int) -> Unit = {},
    title: String = "title",
    defaultValue: Int = 120,
) {
    if (isShow) {
        var text by remember(defaultValue) {
            val value = defaultValue.toString()
            mutableStateOf(TextFieldValue(value, TextRange(value.length)))
        }
        val focusRequest = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            delay(300)
            focusRequest.requestFocus()
        }


        Dialog(onDismissRequest = { onDismiss() }) {
            Column(
                modifier = Modifier
                    .clip(AlertDialogShape)
                    .background(ModalWindowBackgroundColor)
                    .padding(AlertDialogInnerPadding)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    text = title,
                    style = titleTextStyle
                )
                Spacer(modifier = Modifier.height(10.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides MainTextSelectionColor) {
                    BasicTextField(
                        modifier = Modifier
                            .focusRequester(focusRequest)
                            .fillMaxWidth(),
                        value = text,
                        onValueChange = {
                            if (it.text.isDigitsOnly()) {
                                text = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = valueTextStyle.copy(
                            textAlign = TextAlign.Center
                        )
                    ) {
                        it()
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                MAlertButtonsView(
                    buttons = listOf(
                        MAlertButton(stringResource(R.string.metronome_input_save)) {
                            onSuccess(text.text.toIntOrNull() ?: 0)
                            onDismiss()
                        },
                        MAlertButton(stringResource(R.string.metronome_input_cancel)) {
                            onDismiss()
                        }
                    )
                )
            }
        }
    }
}

/** нижняя панель управления */
@Composable
@Preview
private fun BottomControlPanel(
    modifier: Modifier = Modifier,
    intent: (Intent) -> Unit = {},
    isCanSave: Boolean = true,
    isCanDelete: Boolean = true
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
            .padding(BottomControlPadding)
    ) {
        Row {
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_save,
                text = null,
                iconSize = BottomControlButtonSize,
                onClick = { intent(Intent.SaveSong()) },
                isEnable = isCanSave
            )
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_settings,
                text = null,
                iconSize = BottomControlButtonSize,
                onClick = { intent(Intent.IsShowSoundSettings(true)) }
            )
            Spacer(modifier = Modifier.weight(1f))
            MIconButton(
                iconRes = R.drawable.ic_delete,
                text = null,
                iconSize = BottomControlButtonSize,
                onClick = { intent(Intent.IsShowDeleteAlert(true)) },
                isEnable = isCanDelete
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


/** блок для настройки Скорости */
@Composable
@Preview
private fun BmpBlock(
    modifier: Modifier = Modifier,
    bmp: Int = 0,
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MIconButton(
                iconRes = null,
                textSize = controlMetronomeButtonTextSize,
                text = "-5",
                onClick = { intent(Intent.SetSpeed(bmp - 5)) }
            )
            MIconButton(
                iconRes = null,
                textSize = controlMetronomeButtonTextSize,
                text = " - ",
                onClick = { intent(Intent.SetSpeed(bmp - 1)) }
            )
            Column(
                modifier = Modifier.clickable {
                    intent(Intent.IsShowEditBmp(true))
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.metronome_bmp_title), style = titleTextStyle)
                Text(
                    text = bmp.toString(), style = valueTextStyle
                )
            }

            MIconButton(
                iconRes = null,
                text = " + ",
                textSize = controlMetronomeButtonTextSize,
                onClick = { intent(Intent.SetSpeed(bmp + 1)) }
            )
            MIconButton(
                iconRes = null,
                textSize = controlMetronomeButtonTextSize,
                text = "+5",
                onClick = { intent(Intent.SetSpeed(bmp + 5)) }
            )
        }
    }
}

/** блок для настройки Размера Такта */
@Composable
@Preview
private fun TactSizeBlock(
    modifier: Modifier = Modifier,
    tactSize: Int = 0,
    intent: (Intent) -> Unit = {}
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MIconButton(
                iconRes = null,
                text = " - ",
                textSize = controlMetronomeButtonTextSize,
                onClick = { intent(Intent.SetSTactSize(tactSize - 1)) }
            )
            Column(
                modifier = Modifier.clickable {
                    intent(Intent.IsShowEditTactSize(true))
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.metronome_tact_size_title), style = titleTextStyle)
                Text(
                    text = tactSize.toString(), style = valueTextStyle
                )
            }

            MIconButton(
                iconRes = null,
                text = " + ",
                textSize = controlMetronomeButtonTextSize,
                onClick = { intent(Intent.SetSTactSize(tactSize + 1)) }
            )
        }
    }
}


/** панель управления песнями: включить, пауза, переключение песен */
@Composable
@Preview
private fun PlayControlBlock(
    modifier: Modifier = Modifier,
    isPlay: Boolean = true,
    intent: (Intent) -> Unit = {},
    previousSong: SongData? = null,
    nextSong: SongData? = null
) {
    val nextSongImageSize = 50.dp
    val playSongImageSize = 60.dp
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                previousSong?.let {
                    MIconButton(
                        modifier = Modifier
                            .rotate(180f),
                        text = null,
                        iconRes = R.drawable.ic_next_song,
                        iconSize = nextSongImageSize,
                        onClick = { intent(Intent.PreviosSongClick()) }
                    )
                }
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                MIconButton(
                    modifier = Modifier,
                    text = null,
                    iconRes = if (isPlay) R.drawable.ic_pause else R.drawable.ic_play,
                    iconSize = playSongImageSize,
                    onClick = {
                        intent(
                            if (isPlay) Intent.Stop()
                            else Intent.Play()
                        )
                    }
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                nextSong?.let {
                    MIconButton(
                        modifier = Modifier,
                        text = null,
                        iconRes = R.drawable.ic_next_song,
                        iconSize = nextSongImageSize,
                        onClick = { intent(Intent.NextSongClick()) }
                    )
                }
            }
        }
    }
}


/** список доступных звуков.
 *
 * переиспользуемая View для Сильной и для Слабой долей в {[SoundSettingsBottomSheet]}*/
@Composable
@Preview
private fun SoundItems(
    modifier: Modifier = Modifier,
    soundItems: List<String> = emptyList(),
    selectItem: String = "",
    onSelect: (String) -> Unit = {},
    title: String = ""
) {
    Column(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MainTextStyle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            itemsIndexed(soundItems, { index, item -> item.toString() }) { index, item ->
                val isSelect = item == selectItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ButtonShape)
                        .background(if (isSelect) DefaultButtonColor else Color.Transparent)
                        .clickable { onSelect(item) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = SecondTextStyle,
                        color = if (isSelect) ButtonTextColor else SecondTextStyle.color
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}


/** модальное окно настроек звука */
@Composable
@Preview
private fun SoundSettingsBottomSheet(
    isShow: Boolean = false,
    soundList: List<String> = emptyList(),
    mainSound: String = "",
    secondSound: String = "",
    intent: (Intent) -> Unit = {}
) {
    MBottomSheet(
        onDismiss = { intent(Intent.IsShowSoundSettings(false)) },
        isShow = isShow
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.metronome_beat_settings_title),
            style = MainTextStyle,
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(AppBarDividerColor)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            SoundItems(
                modifier = Modifier.weight(1f),
                soundItems = soundList,
                selectItem = mainSound,
                onSelect = { intent(Intent.SetMainSound(it)) },
                title = stringResource(R.string.metronome_downbeat)
            )
            SoundItems(
                modifier = Modifier.weight(1f),
                soundItems = soundList,
                selectItem = secondSound,
                onSelect = { intent(Intent.SetSecondSound(it)) },
                title = stringResource(R.string.metronome_upbeat)
            )
        }
    }
}


/** стили текста для заголовков и значений блоков
 *
 * используется и в UI блоках и в Алерт Диалогов
 *
 * {[BmpBlock]} {[TactSizeBlock]} {[InputAlertDialog]} */
private val valueTextStyle = MainTextStyle.copy(
    fontSize = 40.sp
)
private val titleTextStyle = SecondTextStyle.copy(
    fontSize = 20.sp
)


/** размер текста кнопок +-1/5 в UI блоках {[BmpBlock]} {[TactSizeBlock]} */
private val controlMetronomeButtonTextSize = 30.sp
