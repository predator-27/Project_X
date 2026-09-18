package com.example.projectx.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.theme.*

@Composable
fun EmptyStateCard(
    title: String = "No data found.",
    hint: String? = "Check back later or try refreshing.",
    icon: ImageVector = Icons.Default.Info,
    ctaText: String? = null,
    onCtaClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = PillShape,
                color = InfoBannerBg,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = HeadingNavy,
                textAlign = TextAlign.Center
            )

            if (!hint.isNullOrBlank()) {
                Text(
                    text = hint,
                    fontSize = 13.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
            }

            if (!ctaText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                PrimaryButton(
                    text = ctaText,
                    onClick = onCtaClick
                )
            }
        }
    }
}
