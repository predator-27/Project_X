package com.projectx.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.R

private val SplashBackground = Color(0xFF0A100A)
private val BrandNameColor   = Color(0xFFE8F0D8)
private val TaglineColor     = Color(0xFF7A8870)

/**
 * Optional Project X Brand Pose Composable.
 *
 * Non-blocking brand pose component available for manual preview or fallback.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = "Project X",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Project X",
                color = BrandNameColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Digital Campus Platform",
                color = TaglineColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
