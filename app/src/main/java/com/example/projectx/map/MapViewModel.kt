package com.example.projectx.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val loading: Boolean = true,
    val map: CampusMap? = null,
    val query: String = "",
    val selectedSeatId: String? = null,
    val error: String? = null,
) {
    /** Seats + faculty filtered by [query]. Empty query → returns the full seat list. */
    val filteredSeats: List<Seat>
        get() {
            val m = map ?: return emptyList()
            if (query.isBlank()) return m.vector.seats
            val q = query.trim().lowercase()
            return m.vector.seats.filter { seat ->
                seat.id.lowercase().contains(q)
                    || seat.label.lowercase().contains(q)
                    || seat.badge.lowercase().contains(q)
                    || m.facultyBySeat[seat.id]?.let { f ->
                        f.name.lowercase().contains(q)
                            || f.department.lowercase().contains(q)
                            || f.subjects.any { s -> s.lowercase().contains(q) }
                    } == true
            }
        }

    val selectedSeat: Seat?
        get() = map?.vector?.seats?.firstOrNull { it.id == selectedSeatId }

    val selectedFaculty: Faculty?
        get() = selectedSeatId?.let { map?.facultyBySeat?.get(it) }
}

class MapViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MapRepository(app.applicationContext)

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val map = repo.blockN1()
                _state.value = _state.value.copy(loading = false, map = map)
            } catch (t: Throwable) {
                _state.value = _state.value.copy(loading = false, error = t.message ?: "Failed to load map")
            }
        }
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun select(seatId: String?) {
        _state.value = _state.value.copy(selectedSeatId = seatId)
    }
}
