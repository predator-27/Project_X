package com.example.projectx

import android.net.Uri
import android.widget.VideoView
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

/**
 * Two-phase splash: the video plays first, then a **brand pose** frame
 * lands with our logo before the app takes over. That pose is what tells
 * the user "this is Project X" — it's the thing the video was missing.
 *
 *   Phase.Video       0 - ~5s      video (or the SP.png poster while it loads)
 *   Phase.LogoHold    ~1.4s        logo bloom + brand text + soft glow
 *                                  |─ 250 ms video fades out
 *                                  |─ 400 ms logo scales & fades in (spring)
 *                                  |─ 150 ms brand name fades in
 *                                  |─ 250 ms tagline fades in
 *                                  |─ 400 ms hold
 *   →  onFinished()
 *
 * Safety nets so the user never gets stuck:
 *   - onError → jump straight to LogoHold
 *   - videoTimeoutMs — if the decoder hangs, we skip past it
 */
@Composable
fun SplashScreen(
    videoTimeoutMs: Long = 6_000L,
    logoHoldMs: Long = 1_450L,
    onFinished: () -> Unit,
) {
    var phase by remember { mutableStateOf(Phase.Video) }
    var videoStarted by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }

    fun toLogo() { if (phase == Phase.Video) phase = Phase.LogoHold }
    fun done() { if (!completed) { completed = true; onFinished() } }

    // Fallback — video decoder stalls / crashes
    LaunchedEffect(Unit) {
        delay(videoTimeoutMs)
        toLogo()
    }

    // Logo hold → advance to app
    LaunchedEffect(phase) {
        if (phase == Phase.LogoHold) {
            delay(logoHoldMs)
            done()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SplashBackground)) {

        // Poster paints instantly (SP.png webp) — behind video so the screen
        // is never black while the codec spins up.
        if (!videoStarted) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // ── Video layer ─────────────────────────────────────────
        AnimatedVisibility(
            visible = phase == Phase.Video,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250)),
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
                            start()
                            videoStarted = true
                        }
                        setOnCompletionListener { toLogo() }
                        setOnErrorListener { _, _, _ -> toLogo(); true }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ── Logo pose layer ─────────────────────────────────────
        AnimatedVisibility(
            visible = phase == Phase.LogoHold,
            enter = fadeIn(tween(300)) + scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ),
            exit = fadeOut(tween(300)),
        ) {
            LogoPose()
        }
    }
}

private enum class Phase { Video, LogoHold }

@Composable
private fun LogoPose() {
    // Staggered fade-in for name + tagline so it feels composed, not popped
    var brandVisible by remember { mutableStateOf(false) }
    var tagVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150); brandVisible = true
        delay(250); tagVisible = true
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
        // Soft radial glow — behind the logo, matches Frosted Midnight ambient
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.48f
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
            // Logo — circular white halo behind so the "P" mark reads on any bg
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(HaloTint),
                )
                Image(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = "Project X",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                )
            }
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
                text = "Teacher Desk Portal",
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

// ── Palette (matches Frosted Midnight so splash → app is seamless) ──
private val SplashBackground = Color(0xFF0B1220)          // deep navy — same as SP.png tone
private val GlowPrimary      = Color(0x3360A5FA)          // 20% cornflower for radial glow
private val HaloTint         = Color(0x1AFFFFFF)          // subtle white halo behind logo
private val BrandNameColor   = Color(0xFFF1F5F9)          // near-white — Frosted heading
private val TaglineColor     = Color(0xFF94A3B8)          // Frosted mutedText
