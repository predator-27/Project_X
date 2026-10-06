package com.projectx.app.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.theme.CampusTokens
import com.projectx.app.theme.Danger700
import com.projectx.app.theme.PillShape
import com.projectx.app.theme.Success700
import com.projectx.app.theme.Warning700

enum class StatusTone {
    SUCCESS,
    WARNING,
    DANGER,
    NEUTRAL
}

/**
 * Small coloured chip. Text/background pairs are chosen so each label stays WCAG-AA
 * readable on its own tint in every preset (dark text on light bg for light presets,
 * light text on dark bg for frosted/dark presets). Minimum 12sp per accessibility rules.
 */
@Composable
fun StatusPill(
    text: String,
    tone: StatusTone = StatusTone.SUCCESS,
    modifier: Modifier = Modifier
) {
    val c = CampusTokens.colors
    val bgColor: Color = when (tone) {
        StatusTone.SUCCESS -> c.successGreenBg
        StatusTone.WARNING -> c.warningAmberBg
        StatusTone.DANGER  -> c.dangerRedBg
        StatusTone.NEUTRAL -> c.infoBanner
    }
    val textColor: Color = when (tone) {
        StatusTone.SUCCESS -> if (c.isDark) c.successGreen else Success700
        StatusTone.WARNING -> if (c.isDark) c.warningAmber else Warning700
        StatusTone.DANGER  -> if (c.isDark) c.dangerRed else Danger700
        StatusTone.NEUTRAL -> c.heading
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
