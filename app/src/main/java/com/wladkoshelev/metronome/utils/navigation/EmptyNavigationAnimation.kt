package com.wladkoshelev.metronome.utils.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

/** стиль для анимации (точнее, ее отсутствие) перехода между Фрагментами для библиотеки Raamcosta
 *
 * использование: @Destination(style = EmptyNavigationAnimation::class) */
object EmptyNavigationAnimation : DestinationStyle.Animated {
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition = EnterTransition.None
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition = EnterTransition.None
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition = ExitTransition.None
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition = ExitTransition.None
}