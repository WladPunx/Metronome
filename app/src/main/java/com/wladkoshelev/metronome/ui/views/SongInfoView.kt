package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.database.SongData
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.theme.SecondTextStyle

/** небольшая вьюшка с информацией по Песне {[SongData]}, для сохранения единого стиля
 *
 * используется для отображения Всех Песен и для Создания / Редактирвоания Плейлистов */
@Composable
@Preview
fun SongInfoView(
    modifier: Modifier = Modifier,
    song: SongData = SongData.mock()
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max)
    ) {
        Column {
            Text(text = song.name, style = MainTextStyle)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = stringResource(R.string.bmp_value, song.speed), style = SecondTextStyle)
            Text(text = stringResource(R.string.tact_value, song.tactSize), style = SecondTextStyle)
        }
    }
}


@Composable
@Preview
private fun Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        SongInfoView()
        Text(text = "базовая")

        SongInfoView(modifier = Modifier.fillMaxWidth())
        Text(text = "на всю ширину")
    }
}