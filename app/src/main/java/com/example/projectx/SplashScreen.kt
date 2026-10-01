package com.projectx.app.ui

import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.projectx.app.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFF0A100A)

/**
 * Opening Intro Video Splash Screen.
 *
 * Plays R.raw.splash_animation immediately on app launch preserving aspect ratio
 * (aspect-fit) without stretching or cropping, and transitions into the app/auth flow
 * upon completion.
 */
@Composable
fun SplashScreen(
    videoTimeoutMs: Long = 12_000L,
    onFinished: () -> Unit,
) {
    var completed by remember { mutableStateOf(false) }

    fun done() {
        if (!completed) {
            completed = true
            onFinished()
        }
    }

    // Fallback safety timeout in case video decoder stalls
    LaunchedEffect(Unit) {
        delay(videoTimeoutMs)
        done()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(
                        Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_animation}")
                    )
                    setOnPreparedListener { mp ->
                        mp.setVolume(0f, 0f)
                        mp.isLooping = false
                        // Aspect-fit scaling: preserves aspect ratio without cropping or stretching
                        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                        start()
                    }
                    setOnCompletionListener { done() }
                    setOnErrorListener { _, _, _ ->
                        done()
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
