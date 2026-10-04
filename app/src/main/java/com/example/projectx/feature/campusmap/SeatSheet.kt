package com.projectx.app.feature.campusmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.navmap.model.SearchEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSheet(
    entry: SearchEntry,
    liveDistanceMeters: Double?,
    onRoute: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0D0F1F),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.label,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.fillMaxWidth(0.1f))
                StatusDot(entry.person?.status)
            }
            entry.person?.let { p ->
                Text(p.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                p.designation?.let { Text(it, color = Color(0xBBFFFFFF), fontSize = 12.sp) }
                p.department?.let { Text(it, color = Color(0x99FFFFFF), fontSize = 12.sp) }
                if (p.subjects.isNotEmpty()) {
                    Text(p.subjects.joinToString(" · "), color = Color(0x99FFFFFF), fontSize = 12.sp)
                }
            }
            val distanceLine = liveDistanceMeters?.let { "${it.roundToInt()} m from you" }
                ?: entry.distanceFromEntranceM?.let { "${it.roundToInt()} m from entrance" }
            if (distanceLine != null) {
                Text(distanceLine, color = Color(0xBBFFFFFF), fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRoute, modifier = Modifier.fillMaxWidth()) {
                Text("Navigate")
            }
        }
    }
}

@Composable
private fun StatusDot(status: String?) {
    val color = when (status?.lowercase()) {
        "available" -> MapTheme.StatusAvailable
        "busy" -> MapTheme.StatusBusy
        "away" -> MapTheme.StatusAway
        else -> Color.Transparent
    }
    if (color != Color.Transparent) {
        Surface(
            shape = CircleShape,
            color = color,
            modifier = Modifier.size(10.dp),
        ) {}
    }
}
