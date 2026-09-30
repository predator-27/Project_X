package com.projectx.app.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.FrostedSurface
import com.projectx.app.theme.CampusTokens

/**
 * Full-screen indoor map for Block N1.
 *
 * Layout:
 *   - Top-right: menu icon (opens drawer via [onMenuClick])
 *   - Top-center: floating frosted search bar
 *   - Full-bleed [MapCanvas]
 *   - Bottom: filtered seat list (when searching) OR floor-legend chip
 *   - Modal bottom sheet: seat details when a seat is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onMenuClick: () -> Unit,
    targetSeatId: String? = null,
    modifier: Modifier = Modifier,
    vm: MapViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val c = CampusTokens.colors

    LaunchedEffect(targetSeatId, state.map) {
        if (!targetSeatId.isNullOrBlank() && state.map != null) {
            vm.select(targetSeatId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Canvas (full-bleed, behind everything else)
        state.map?.let { m ->
            MapCanvas(
                map = m,
                selectedSeatId = state.selectedSeatId,
                onSeatTap = { vm.select(it) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Loading spinner
        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.primary)
            }
        }

        // Error state
        state.error?.let { err ->
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                FrostedSurface {
                    Text(
                        text = "Map failed to load: $err",
                        color = c.dangerRed,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        // Top bar overlay: menu + search
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FrostedSurface {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = c.heading)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    SearchField(
                        value = state.query,
                        onValueChange = vm::setQuery,
                    )
                }
            }

            // Search results — floating list under the bar
            val hits = state.filteredSeats
            if (state.query.isNotBlank() && state.map != null) {
                FrostedSurface(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightMax(280.dp),
                        contentPadding = PaddingValues(vertical = 6.dp),
                    ) {
                        items(hits.take(30), key = { it.id }) { seat ->
                            SearchRow(
                                seat = seat,
                                faculty = state.map!!.facultyBySeat[seat.id],
                                onClick = {
                                    vm.select(seat.id)
                                    vm.setQuery("")
                                },
                            )
                        }
                        if (hits.isEmpty()) {
                            item {
                                Text(
                                    text = "No seat or faculty matches \"${state.query}\".",
                                    color = c.mutedText,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floor legend chip — bottom-left
        state.map?.let { m ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
            ) {
                FrostedSurface {
                    Text(
                        text = "${m.vector.name} · Floor ${m.vector.floor} · ${m.vector.seatCounts.total} seats",
                        color = c.mutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        // Selected-seat bottom sheet
        if (state.selectedSeatId != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { vm.select(null) },
                sheetState = sheetState,
                containerColor = c.surface,
            ) {
                SeatDetail(
                    seat = state.selectedSeat!!,
                    faculty = state.selectedFaculty,
                )
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    val c = CampusTokens.colors
    FrostedSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = c.mutedText, modifier = Modifier.size(18.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "Search seat, faculty, subject…",
                        color = c.mutedText,
                        fontSize = 14.sp,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = c.heading, fontSize = 14.sp),
                    cursorBrush = SolidColor(c.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = c.mutedText, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchRow(seat: Seat, faculty: Faculty?, onClick: () -> Unit) {
    val c = CampusTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(
                if (faculty != null) c.primary.copy(alpha = 0.25f) else c.surfaceElevated,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(seat.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = c.heading)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = faculty?.name ?: seat.id,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.heading,
            )
            Text(
                text = if (faculty != null) "${seat.id} · ${faculty.department}"
                       else "Unassigned seat · ${seat.kind}",
                fontSize = 11.sp,
                color = c.mutedText,
            )
        }
    }
}

@Composable
private fun SeatDetail(seat: Seat, faculty: Faculty?) {
    val c = CampusTokens.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (faculty != null) c.primary else c.surfaceElevated),
                contentAlignment = Alignment.Center,
            ) {
                Text(seat.badge, fontWeight = FontWeight.Bold, color = if (faculty != null) Color.White else c.heading)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(faculty?.name ?: seat.id, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.heading)
                Text(
                    text = faculty?.designation ?: seat.kind.replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = c.mutedText,
                )
            }
        }
        if (faculty != null) {
            KV("Department", faculty.department)
            KV("Status", faculty.status)
            KV("Office hours", faculty.hours)
            KV("Email", faculty.email)
            if (faculty.subjects.isNotEmpty()) {
                KV("Subjects", faculty.subjects.joinToString(", "))
            }
        } else {
            KV("Seat", seat.id)
            KV("Kind", seat.kind)
            KV("Zone", seat.zone)
        }
        Box(Modifier.height(8.dp))
    }
}

@Composable
private fun KV(label: String, value: String) {
    val c = CampusTokens.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = c.mutedText)
        Text(value, fontSize = 13.sp, color = c.bodyText, fontWeight = FontWeight.Medium)
    }
}

/** Kotlin doesn't ship heightMax in Compose — small helper to cap LazyColumn height. */
private fun Modifier.heightMax(max: androidx.compose.ui.unit.Dp): Modifier =
    this.then(Modifier.height(max))
