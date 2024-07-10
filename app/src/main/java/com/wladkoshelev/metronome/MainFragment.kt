package com.wladkoshelev.metronome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.ui.metronome.getMetronomeFragment
import com.wladkoshelev.metronome.ui.playlist.getPlayListsFragment
import com.wladkoshelev.metronome.ui.songs.getAllSongsFragment
import com.wladkoshelev.metronome.utils.NavigationInstance.Companion.myNavigate

@RootNavGraph(start = true)
@Destination
@Composable
fun MainFragment(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { navController.myNavigate(getMetronomeFragment()) }) {
            Text(text = "метраном")
        }
        Button(onClick = { navController.myNavigate(getAllSongsFragment()) }) {
            Text(text = "все песни")
        }
        Button(onClick = { navController.myNavigate(getPlayListsFragment()) }) {
            Text(text = "плейлисты")
        }
    }
}

