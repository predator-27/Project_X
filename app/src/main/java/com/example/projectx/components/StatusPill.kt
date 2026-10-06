package com.projectx.app.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.theme.*

enum class StatusTone {
    SUCCESS,
    WARNING,
    DANGER,
    NEUTRAL
}

@Composable
fun StatusPill(
    text: String,
    tone: StatusTone = StatusTone.SUCCESS,
    modifier: Modifier = Modifier
) {
    val bgColor = when (tone) {
        StatusTone.SUCCESS -> SecondaryEmeraldBg
        StatusTone.WARNING -> WarningAmberBg
        StatusTone.DANGER -> AccentCoralBg
        StatusTone.NEUTRAL -> InfoBannerBg
    }
    val textColor = when (tone) {
        StatusTone.SUCCESS -> SecondaryEmerald
        StatusTone.WARNING -> WarningAmber
        StatusTone.DANGER -> AccentCoral
        StatusTone.NEUTRAL -> HeadingNavy
    }

    Surface(
        shape = PillShape,
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
