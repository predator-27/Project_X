package com.projectx.app.feature.campusmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.navmap.model.SearchEntry
import kotlin.math.roundToInt

@Composable
fun CampusMapScreen(
    modifier: Modifier = Modifier,
    viewModel: CampusMapViewModel = viewModel(),
    onClose: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    Box(modifier = modifier.fillMaxSize().background(MapTheme.Background)) {
        when (val s = state) {
            is CampusMapUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MapTheme.UserRim,
            )
            is CampusMapUiState.Error -> Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Map unavailable", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(s.message, color = Color(0xAAFFFFFF), style = MaterialTheme.typography.bodySmall)
            }
            is CampusMapUiState.Loaded -> LoadedBody(
                state = s,
                onSearch = viewModel::onSearchChanged,
                onPickResult = { viewModel.onSelectPoi(it.id) },
                onPayload = { viewModel.onQrPayload(it) },
                onClearBanner = viewModel::clearScanBanner,
                onClearRoute = viewModel::clearRoute,
                onExit = viewModel::routeToExit,
                onClose = onClose,
            )
        }
    }
}

@Composable
private fun LoadedBody(
    state: CampusMapUiState.Loaded,
    onSearch: (String) -> Unit,
    onPickResult: (SearchEntry) -> Unit,
    onPayload: (com.projectx.app.navmap.qr.QrPayload) -> Unit,
    onClearBanner: () -> Unit,
    onClearRoute: () -> Unit,
    onExit: () -> Unit,
    onClose: (() -> Unit)?,
) {
    var showScanner by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MapCanvas(
            campus = state.campus,
            modifier = Modifier.fillMaxSize(),
            overlay = { t ->
                drawRouteOverlay(
                    campus = state.campus,
                    transform = t,
                    route = state.route,
                    kind = state.routeKind,
                    destinationPoiId = state.selectedPoiId,
                    userScreen = state.user?.let { t.cellCenterScreen(it.cellRow.toInt(), it.cellCol.toInt()) },
                )
                state.user?.let { u ->
                    val p = t.cellCenterScreen(u.cellRow.toInt(), u.cellCol.toInt())
                    drawUserMarker(p, t.cellPixels * 0.9f, u.headingDeg)
                }
            },
        )

        // Top chrome: search + close
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; onSearch(it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Search seat, person, subject…", color = Color(0x99FFFFFF)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xBBFFFFFF)) },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { showScanner = true },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MapTheme.UserRim),
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = Color.Black)
                }
                onClose?.let {
                    IconButton(onClick = it) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            if (state.search.isNotEmpty()) {
                ElevatedCard(
                    colors = androidx.compose.material3.CardDefaults.elevatedCardColors(containerColor = Color(0xF00D0F1F)),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(state.search, key = { it.id }) { entry ->
                            SearchRow(entry) {
                                onPickResult(entry)
                                query = ""
                            }
                        }
                    }
                }
            }
        }

        // Scan banner
        state.lastScanBanner?.let { msg ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
                    .fillMaxWidth(0.9f),
                color = Color(0xE60D0F1F),
                shape = RoundedCornerShape(10.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = onClearBanner) { Text("Dismiss", color = MapTheme.UserRim) }
                }
            }
        }

        // Bottom HUD: route distance + clear + exit
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.95f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.route.found) {
                Surface(
                    color = Color(0xE60D0F1F),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${state.route.distanceMeters.roundToInt()} m to destination",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        state.route.steps.take(2).forEach {
                            Text(it.instruction, color = Color(0xBBFFFFFF), fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = onClearRoute) { Text("Clear", color = MapTheme.UserRim) }
                        }
                    }
                }
            } else {
                TextButton(onClick = onExit) {
                    Text("Route to exit", color = MapTheme.RouteExit, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showScanner) {
            QrScanScreen(
                onPayload = { payload ->
                    showScanner = false
                    onPayload(payload)
                },
                onCancel = { showScanner = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SearchRow(entry: SearchEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextButton(onClick = onClick, modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                entry.person?.let { Text(it.name, color = Color(0x99FFFFFF), fontSize = 12.sp) }
            }
            entry.distanceFromEntranceM?.let {
                Text("${it.roundToInt()} m", color = Color(0x99FFFFFF), fontSize = 12.sp)
            }
        }
    }
}

