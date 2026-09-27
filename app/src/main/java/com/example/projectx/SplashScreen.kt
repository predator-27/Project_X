package com.example.projectx

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

/**
 * Cold-launch splash sequence:
 *
 *   1. SP.png webp paints INSTANTLY behind everything so the screen
 *      is never black — the deep-navy of the splash matches the app
 *      background so any letterbox from the video stays invisible.
 *   2. VideoView loads `res/raw/splash_animation.mp4` and starts playing;
 *      once it reports "started" we hide the poster and the video owns the
 *      pixels.
 *   3. Video onCompletion → [onFinished] → app takes over.
 *
 * Two safety nets so the user never gets stuck on the splash:
 *   - `onError` returns true and calls [onFinished] immediately.
 *   - 7-second timeout — if the video decoder stalls, we advance anyway.
 */
@Composable
fun SplashScreen(
    timeoutMs: Long = 7_000L,
    onFinished: () -> Unit,
) {
    var completed by remember { mutableStateOf(false) }
    var videoStarted by remember { mutableStateOf(false) }

    fun advance() {
        if (!completed) {
            completed = true
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(timeoutMs)
        advance()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
    ) {
        // Poster — fades away the moment the video actually starts drawing frames.
        if (!videoStarted) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(
                        Uri.parse("android.resource://${context.packageName}/${R.raw.splash_animation}")
                    )
                    setOnPreparedListener { mp ->
                        mp.setVolume(0f, 0f)      // splash is silent by design
                        mp.isLooping = false
                        start()
                        videoStarted = true
                    }
                    setOnCompletionListener { advance() }
                    setOnErrorListener { _, _, _ ->
                        advance()
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Matches SP.png tone + Frosted Midnight `#0B1220` so no visual jump. */
private val SplashBackground = Color(0xFF0B1220)
