package com.example.projectx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Animated Project X Brand Splash Screen.
 *
 * Displays the dark navy background with radial glow, Project X brand logo,
 * "Project X" title, and "Digital Campus Platform" tagline before advancing to the app shell.
 */
@Composable
fun SplashScreen(
    logoHoldMs: Long = 1_850L,
    onFinished: () -> Unit,
) {
    var completed by remember { mutableStateOf(false) }

    fun done() {
        if (!completed) {
            completed = true
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        delay(logoHoldMs)
        done()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        LogoPose()
    }
}

@Composable
private fun LogoPose() {
    var brandVisible by remember { mutableStateOf(false) }
    var tagVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        brandVisible = true
        delay(250)
        tagVisible = true
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
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        // Soft radial glow — matches Frosted Midnight palette
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
            // Project X Logo
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
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
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

// ── Palette (matches Frosted Midnight so splash → app is seamless) ──
private val SplashBackground = Color(0xFF0B1220)          // deep navy
private val GlowPrimary      = Color(0x3360A5FA)          // 20% cornflower for radial glow
private val HaloTint         = Color(0x1AFFFFFF)          // subtle white halo behind logo
private val BrandNameColor   = Color(0xFFF1F5F9)          // near-white heading
private val TaglineColor     = Color(0xFF94A3B8)          // mutedText
