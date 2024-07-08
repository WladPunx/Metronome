package com.wladkoshelev.metronome.ui_metranome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.destinations.MetronomeFragmentDestination
import com.wladkoshelev.metronome.utils.NavigationInstance

@RootNavGraph
@Destination
@Composable
fun MetronomeFragment() {
    UI()
}

fun getMetronomeFragment() = NavigationInstance(MetronomeFragmentDestination())


@Composable
@Preview
private fun UI() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = { }) {
            Text(text = "ппппп")
        }
    }
}

