package com.projectx.app.feature.campusmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.navmap.loader.AssetJsonSource
import com.projectx.app.navmap.loader.MapLoadResult
import com.projectx.app.navmap.loader.MapRepository
import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Cell
import com.projectx.app.navmap.model.SearchEntry
import com.projectx.app.navmap.qr.QrPayload
import com.projectx.app.navmap.qr.QrPayloadParser
import com.projectx.app.navmap.qr.QrResult
import com.projectx.app.navmap.routing.Route
import com.projectx.app.navmap.routing.RouteBuilder
import com.projectx.app.navmap.search.PoiSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface CampusMapUiState {
    data object Loading : CampusMapUiState
    data class Error(val message: String) : CampusMapUiState
    data class Loaded(
        val campus: CampusMap,
        val user: NavUserState? = null,
        val search: List<SearchEntry> = emptyList(),
        val selectedPoiId: String? = null,
        val route: Route = Route.Empty,
        val routeKind: RouteKind = RouteKind.ToPerson,
        val lastScanBanner: String? = null,
    ) : CampusMapUiState
}

class CampusMapViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MapRepository(AssetJsonSource(app))

    private val _state = MutableStateFlow<CampusMapUiState>(CampusMapUiState.Loading)
    val state: StateFlow<CampusMapUiState> = _state.asStateFlow()

    init { load("N1") }

    fun load(mapId: String) {
        _state.value = CampusMapUiState.Loading
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repository.load(mapId) }
            _state.value = when (result) {
                is MapLoadResult.Loaded -> CampusMapUiState.Loaded(result.map)
                is MapLoadResult.Invalid -> CampusMapUiState.Error(result.reason)
            }
        }
    }

    fun onSearchChanged(query: String) {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return
        val hits = PoiSearch.query(loaded.campus.searchIndex, query)
        _state.value = loaded.copy(search = hits)
    }

    fun onSelectPoi(id: String) {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return
        val from = currentFromCell(loaded) ?: return
        val route = RouteBuilder.toPoi(loaded.campus, from, id)
        val kind = if (loaded.campus.grid.anchorById[id] != null) RouteKind.ToExit else RouteKind.ToPerson
        _state.value = loaded.copy(selectedPoiId = id, route = route, routeKind = kind, search = emptyList())
    }

    fun routeToExit() {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return
        val from = currentFromCell(loaded) ?: return
        val exit = loaded.campus.grid.anchorById["N1-A01"] ?: return
        val route = RouteBuilder.toAnchor(loaded.campus, from, exit.anchorId)
        _state.value = loaded.copy(selectedPoiId = exit.anchorId, route = route, routeKind = RouteKind.ToExit)
    }

    fun clearRoute() {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return
        _state.value = loaded.copy(selectedPoiId = null, route = Route.Empty)
    }

    fun onQrScanned(raw: String?): String? {
        val loaded = _state.value as? CampusMapUiState.Loaded
            ?: return "Map still loading"
        val parsed = QrPayloadParser.parse(raw)
        if (parsed is QrResult.Invalid) {
            _state.value = loaded.copy(lastScanBanner = parsed.reason)
            return parsed.reason
        }
        val payload = (parsed as QrResult.Valid).payload
        val verified = QrPayloadParser.verifyAgainst(payload, loaded.campus.grid)
        if (verified is QrResult.Invalid) {
            _state.value = loaded.copy(lastScanBanner = verified.reason)
            return verified.reason
        }
        return applyPayload(loaded, payload)
    }

    fun onQrPayload(payload: QrPayload): String? {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return "Map still loading"
        val verified = QrPayloadParser.verifyAgainst(payload, loaded.campus.grid)
        if (verified is QrResult.Invalid) {
            _state.value = loaded.copy(lastScanBanner = verified.reason)
            return verified.reason
        }
        return applyPayload(loaded, payload)
    }

    private fun applyPayload(loaded: CampusMapUiState.Loaded, payload: QrPayload): String? {
        val user = NavUserState(
            cellRow = payload.y.toDouble(),
            cellCol = payload.x.toDouble(),
            headingDeg = payload.headingDeg.toDouble(),
        )
        val from = Cell(payload.x, payload.y)
        val route = loaded.selectedPoiId?.let { id ->
            if (loaded.campus.grid.anchorById[id] != null) RouteBuilder.toAnchor(loaded.campus, from, id)
            else RouteBuilder.toPoi(loaded.campus, from, id)
        } ?: Route.Empty
        _state.value = loaded.copy(
            user = user,
            route = route,
            lastScanBanner = "Located at ${loaded.campus.grid.anchorById[payload.anchorId]?.label ?: payload.anchorId}",
        )
        return null
    }

    fun clearScanBanner() {
        val loaded = _state.value as? CampusMapUiState.Loaded ?: return
        _state.value = loaded.copy(lastScanBanner = null)
    }

    private fun currentFromCell(loaded: CampusMapUiState.Loaded): Cell? {
        loaded.user?.let { return Cell(it.cellCol.toInt(), it.cellRow.toInt()) }
        return loaded.campus.grid.anchorById["N1-A02"]?.cell
    }
}
