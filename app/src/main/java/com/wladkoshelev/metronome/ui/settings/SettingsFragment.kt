package com.wladkoshelev.metronome.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.wladkoshelev.metronome.BuildConfig
import com.wladkoshelev.metronome.R
import com.wladkoshelev.metronome.destinations.SettingsFragmentDestination
import com.wladkoshelev.metronome.theme.DividerColor
import com.wladkoshelev.metronome.theme.MainTextStyle
import com.wladkoshelev.metronome.ui.views.EditableFragmentTitle
import com.wladkoshelev.metronome.utils.navigation.NavigationInstance


fun getSettingsFragment() = NavigationInstance(SettingsFragmentDestination())

@RootNavGraph
@Destination
@Composable
fun SettingsFragment(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val context = LocalContext.current
        EditableFragmentTitle(
            title = stringResource(R.string.settings_title)
        )
        Item(
            text = stringResource(R.string.settings_language),
            onClick = {}
        )
        Item(
            text = stringResource(R.string.settings_download_apk),
            onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/WladPunx/MetronomeAPI/releases")
                    )
                )
            }
        )
        Item(
            text = stringResource(R.string.settings_write_to_support),
            onClick = {
                val body = """
                AppVersion: ${BuildConfig.VERSION_NAME}
                DeviceSDK: ${Build.VERSION.SDK_INT}
                Model: ${Build.MANUFACTURER} / ${Build.MODEL}
                .................
                Text
            """.trimIndent()
                val uri = Uri.parse("mailto:vladkoshelev13@gmail.com?&subject=Support Metronome&body=$body")
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = "v.${BuildConfig.VERSION_NAME}",
            style = MainTextStyle
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
@Preview
private fun Item(
    text: String = "text",
    onClick: () -> Unit = {}
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                text = text,
                style = MainTextStyle,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor)
        )
    }
}


