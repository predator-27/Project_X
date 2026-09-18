package com.example.projectx.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.theme.*

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
    val (bgColor, textColor) = when (tone) {
        StatusTone.SUCCESS -> SecondaryEmeraldBg to SecondaryEmerald
        StatusTone.WARNING -> WarningAmberBg to WarningAmber
        StatusTone.DANGER -> AccentCoralBg to AccentCoral
        StatusTone.NEUTRAL -> NavySidebarActive to HeadingNavy
    }

    Surface(
        shape = PillShape,
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
