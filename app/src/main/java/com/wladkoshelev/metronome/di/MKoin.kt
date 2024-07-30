package com.wladkoshelev.metronome.di

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.wladkoshelev.metronome.MetronomeLDS
import com.wladkoshelev.metronome.database.SongREP
import com.wladkoshelev.metronome.database.SongsDB
import com.wladkoshelev.metronome.database.SongsLDS
import com.wladkoshelev.metronome.ui.metronome.MetronomeVM
import com.wladkoshelev.metronome.ui.playlist.CreateOrEditPlayListVM
import com.wladkoshelev.metronome.ui.playlist.PlayListsVM
import com.wladkoshelev.metronome.ui.songs.AllSongsVM
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.ParametersDefinition

object MKoin {

    fun start(apl: Application) {
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(apl)
            modules(
                MetronomeLDS().mModule(),
                MetronomeVM().mModule(),


                SongsDB().mModule(),
                SongsLDS().mModule(),
                SongREP().mModule(),


                AllSongsVM().mModule(),


                PlayListsVM().mModule(),
                CreateOrEditPlayListVM().mModule()
            )
        }
    }

    @Composable
    inline fun <reified T : ViewModel> NavController.sharedViewModel(
        noinline parameters: ParametersDefinition? = null
    ): T {
        val navGraphRoute = currentBackStackEntry?.destination?.parent?.route ?: return koinViewModel(parameters = parameters)
        val parentEntry = remember(currentBackStackEntry) {
            getBackStackEntry(navGraphRoute)
        }
        return koinViewModel(viewModelStoreOwner = parentEntry, parameters = parameters)
    }

}