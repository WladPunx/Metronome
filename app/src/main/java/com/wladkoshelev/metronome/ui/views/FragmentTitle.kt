package com.wladkoshelev.metronome.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wladkoshelev.metronome.theme.AppBarColor
import com.wladkoshelev.metronome.theme.AppBarDividerColor
import com.wladkoshelev.metronome.theme.MainTextStyle

/** кастомный АппБар с название экрана и небольшим отступом после, чтобы сохранить общий дизайн отступа для всех экранов */
@Composable
@Preview
fun FragmentTitle(
    title: String = "title"
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppBarColor)
                .padding(top = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MainTextStyle,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(AppBarDividerColor)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
    }

}