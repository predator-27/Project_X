package com.projectx.app.ui

import com.projectx.app.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Two-phase Project X splash:
 *
 *   Phase.Poster   ~1.2 s   full-bleed SP.png (deep-navy hero image)
 *   Phase.LogoHold ~1.8 s   LO.png brand mark + "Project X" + tagline
 *                           on the same navy, with a soft radial glow.
 *
 * The whole thing sits on `SplashBackground` (#0B1220), which is exactly
 * the tone of both SP.png and the Frosted Midnight app background —
 * so the cross-fade between phases is invisible and the hand-off to the
 * app is seamless.
 */
@Composable
fun SplashScreen(
    posterHoldMs: Long = 1_200L,
    logoHoldMs: Long = 1_800L,
    onFinished: () -> Unit,
) {
    var phase by remember { mutableStateOf(Phase.Poster) }
    var completed by remember { mutableStateOf(false) }

    fun toLogo() { if (phase == Phase.Poster) phase = Phase.LogoHold }
    fun done() { if (!completed) { completed = true; onFinished() } }

    // Poster → advance to logo hold
    LaunchedEffect(Unit) {
        delay(posterHoldMs)
        toLogo()
    }

    // Logo hold → finish
    LaunchedEffect(phase) {
        if (phase == Phase.LogoHold) {
            delay(logoHoldMs)
            done()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        // Phase 1 — SP.png hero
        AnimatedVisibility(
            visible = phase == Phase.Poster,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(400)),
        ) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Phase 2 — LO.png brand pose
        AnimatedVisibility(
            visible = phase == Phase.LogoHold,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.90f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ),
            exit = fadeOut(tween(350)),
        ) {
            LogoPose()
        }
    }
}

private enum class Phase { Poster, LogoHold }

@Composable
private fun LogoPose() {
    var brandVisible by remember { mutableStateOf(false) }
    var tagVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); brandVisible = true
        delay(280); tagVisible = true
    }

    val brandAlpha by animateFloatAsState(
        targetValue = if (brandVisible) 1f else 0f,
        animationSpec = tween(350),
        label = "brand",
    )
    val tagAlpha by animateFloatAsState(
        targetValue = if (tagVisible) 1f else 0f,
        animationSpec = tween(350),
        label = "tag",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        // Soft radial glow — Frosted Midnight ambient hue
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.46f
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(GlowPrimary, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = size.minDimension * 0.55f,
                ),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Logo — native aspect ratio (source PNG drives shape).
            // No halo, no square container — just the mark on the ambient bg.
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = "Project X",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(190.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Project X",
                color = BrandNameColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(brandAlpha),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Digital Campus Platform",
                color = TaglineColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(tagAlpha),
            )
        }
    }
}

// ── Palette — Pixel Glass Green (matches app theme so splash → app is seamless) ──
private val SplashBackground = Color(0xFF0A100A)   // near-black olive (= pageBackground)
private val GlowPrimary      = Color(0x40A8B840)   // 25% mosaic green radial
private val HaloTint         = Color(0x14FFFFFF)   // barely-there white halo
private val BrandNameColor   = Color(0xFFE8F0D8)   // warm off-white, green cast
private val TaglineColor     = Color(0xFF7A8870)   // muted olive
