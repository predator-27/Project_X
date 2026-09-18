package com.example.projectx.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectx.theme.*

@Composable
fun DataTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                headers.forEach { header ->
                    Text(
                        text = header.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedText,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .width(140.dp)
                            .padding(end = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            // Data Rows
            rows.forEachIndexed { index, rowData ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowData.forEach { cell ->
                        Text(
                            text = cell,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = BodyText,
                            modifier = Modifier
                                .width(140.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }
                if (index < rows.size - 1) {
                    HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.5f), thickness = 1.dp)
                }
            }
        }
    }
}
