package com.wladkoshelev.metronome.utils

import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction

data class NavigationInstance(
    val destination: Direction
) {
    companion object {
        fun NavController.myNavigate(
            instance: NavigationInstance,
            isSingle: Boolean = true
        ) {
            this.navigate(instance.destination, navOptionsBuilder = {
                if (isSingle) {
                    popUpTo(instance.destination.route) {
                        this.inclusive = true
                    }
                    launchSingleTop = true
                }
            })
        }
    }
}