package com.wladkoshelev.metronome.ui.tap_the_beat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.theme.BottomControlButtonSize
import com.wladkoshelev.metronome.theme.ButtonTextColor
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.theme.SecondTextStyle
import com.wladkoshelev.metronome.ui.tap_the_beat.TapTheBeatComponent.Intent
import com.wladkoshelev.metronome.ui.tap_the_beat.TapTheBeatComponent.State
import com.wladkoshelev.metronome.ui.views.MBottomSheet
import com.wladkoshelev.metronome.ui.views.MIconButton
import kotlinx.coroutines.flow.filter

/** вьюшка модалки с возможностью настучать бит */
@Composable
fun TapTheBeatView(
    component: TapTheBeatComponent
) {
    val state: State by component.state.collectAsStateWithLifecycle()
    val intent: (Intent) -> Unit by remember { mutableStateOf(component::sendIntent) }
    MBottomSheet(
        isShow = state.isShow,
        onDismiss = { intent(Intent.IsShow(false)) }
    ) {
        UI(
            state = state,
            intent = intent
        )
    }
}


@Composable
@Preview
private fun UI(
    state: State = State(),
    intent: (Intent) -> Unit = {}
) {
    val mShape by remember { mutableStateOf(RoundedCornerShape(20.dp)) }
    val textColor by remember { mutableStateOf(ButtonTextColor) }
    val isWorkColor by remember { mutableStateOf(Color(0xFF0F990F)) }
    val isNotWorkColor by remember { mutableStateOf(Color(0xFFB91414)) }

    /** обработку клика на Бит делаем здесь, потому что по умолчанию клик срабатывает только когда отпускаешь палец
     *
     * поэтому фильтруем события именно нажатия, чтобы сократить погрешность задержки нажатого пальца */
    val beatInteractionSource = remember { MutableInteractionSource() }
    LaunchedEffect(Unit) {
        beatInteractionSource.interactions
            .filter { it is PressInteraction.Press }
            .collect {
                intent(Intent.BeatIt())
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(350.dp)
                .clip(mShape)
                .border(5.dp, DividerColor, mShape)
                .background(
                    animateColorAsState(
                        targetValue = if (state.isWork) isWorkColor else isNotWorkColor,
                        label = "",
                        animationSpec = tween(durationMillis = 700)
                    ).value
                )
                .clickable(
                    interactionSource = beatInteractionSource,
                    indication = LocalIndication.current,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val speedText = if (state.speed > 0) state.speed.toString() else ""
                Text(text = stringResource(R.string.tap_the_beat_help_text), style = SecondTextStyle, color = textColor, fontSize = 25.sp)
                Text(text = speedText, style = MainTextStyle, fontSize = 50.sp, color = textColor)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        MIconButton(
            iconRes = R.drawable.ic_save,
            text = null,
            iconSize = BottomControlButtonSize,
            onClick = { intent(Intent.ApplyNewBeat()) }
        )
        Spacer(modifier = Modifier.height(50.dp))
    }
}