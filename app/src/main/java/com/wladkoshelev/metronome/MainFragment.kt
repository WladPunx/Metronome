package com.wladkoshelev.metronome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.ui.metronome.getMetronomeFragment
import com.wladkoshelev.metronome.ui.playlist.getPlayListsFragment
import com.wladkoshelev.metronome.ui.settings.getSettingsFragment
import com.wladkoshelev.metronome.ui.songs.getSongsFragment
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance.Companion.myNavigate


@RootNavGraph(start = true)
@Destination
@Composable
fun MainFragment(navController: NavController) {
    val navigateTo: (NavigationInstance) -> Unit by remember { mutableStateOf({ navController.myNavigate(it) }) }
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { navigateTo(getMetronomeFragment()) }) {
            Text(text = "метраном")
        }
        Button(onClick = { navigateTo(getSongsFragment()) }) {
            Text(text = "все песни")
        }
        Button(onClick = { navigateTo(getPlayListsFragment()) }) {
            Text(text = "плейлисты")
        }
        Button(onClick = { navigateTo(getSettingsFragment()) }) {
            Text(text = "--настройки")
        }
    }
}

