package com.nuvio.tv.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import coil3.compose.AsyncImage
import com.nuvio.tv.R
import kotlinx.coroutines.delay

/** The original Kodi-era Titanium X reveal, bundled locally for offline startup. */
@Composable
fun TitaniumIntroOverlay(enabled: Boolean) {
    var finished by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    if (!enabled || finished) return

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(6_000)
        finished = true
    }
    BackHandler { finished = true }
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    finished = true
                    true
                } else true
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        AsyncImage(
            model = R.raw.titanium_intro,
            contentDescription = "Titanium X cinematic intro",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "Press OK to skip",
            color = Color.White.copy(alpha = 0.75f),
            modifier = Modifier.align(Alignment.BottomEnd).padding(28.dp)
        )
    }
}
