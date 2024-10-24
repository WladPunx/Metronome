package com.wladkoshelev.metronome.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.destinations.LangFragmentDestination
import com.wladkoshelev.metronome.ui.settings.LangEnum.Companion.setLang
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.ui.views.MIconButton
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance

fun getLangFragment(isCanBack: Boolean) = NavigationInstance(LangFragmentDestination(isCanBack = isCanBack))


@RootNavGraph
@Destination
@Composable
fun LangFragment(
    navController: NavController,
    /** параметр "надо ли включать блок на клик Назад? */
    isCanBack: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (isCanBack.not()) {
            BackHandler {}
        }
        val context = LocalContext.current
        EditableFragmentTitle(
            title = stringResource(R.string.settings_language)
        )

        val langList by remember { mutableStateOf(LangEnum.values().toList()) }
        langList.forEach {
            MIconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                text = it.title,
                iconRes = null,
                onClick = {
                    context.setLang(it)
                    navController.popBackStack()
                }
            )
        }
    }
}