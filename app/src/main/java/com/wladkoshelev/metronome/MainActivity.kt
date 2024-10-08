package com.wladkoshelev.metronome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.wladkoshelev.metronome.theme.MetronomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetronomeTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        engine = rememberNavHostEngine(
                            rootDefaultAnimations = RootNavGraphDefaultAnimations(
                                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                            )
                        )
                    )
                }
            }
        }
    }
}