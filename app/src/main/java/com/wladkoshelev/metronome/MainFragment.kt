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
import com.wladkoshelev.metronome.ui.metronome.MetronomeFragment
import com.wladkoshelev.metronome.ui.playlist.PlayListsFragment
import com.wladkoshelev.metronome.ui.songs.AllSongsFragment
import com.wladkoshelev.metronome.utils.NavigationInstance.Companion.myNavigate


class MainFragment {
    companion object {
        @RootNavGraph(start = true)
        @Destination
        @Composable
        fun MainFragment(navController: NavController) {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(onClick = { navController.myNavigate(MetronomeFragment.get()) }) {
                    Text(text = "метраном")
                }
                Button(onClick = { navController.myNavigate(AllSongsFragment.get()) }) {
                    Text(text = "все песни")
                }
                Button(onClick = { navController.myNavigate(PlayListsFragment.get()) }) {
                    Text(text = "плейлисты")
                }
            }
        }
    }
}

