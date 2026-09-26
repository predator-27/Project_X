package com.example.projectx

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

/**
 * Full-bleed splash rendered from `res/drawable-nodpi/splash.webp`.
 * Shows for [durationMs] then invokes [onFinished] — the parent swaps
 * itself out for the real UI.
 */
@Composable
fun SplashScreen(
    durationMs: Long = 1500L,
    onFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(durationMs)
        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
