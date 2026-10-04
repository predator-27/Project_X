package com.projectx.app.navmap.loader

import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.FacultyDirectory
import com.projectx.app.navmap.model.MapGrid
import com.projectx.app.navmap.model.SearchIndex
import com.projectx.app.navmap.model.VectorMap
import kotlinx.serialization.json.Json

sealed class MapLoadResult {
    data class Loaded(val map: CampusMap) : MapLoadResult()
    data class Invalid(val reason: String) : MapLoadResult()
}

/**
 * Pure-Kotlin loader for one map bundle (e.g. "N1"). Reads the four JSON files
 * through a [JsonSource], validates the data, and caches the result in memory.
 *
 * No Android imports here on purpose — the Android asset adapter is a separate class.
 */
class MapRepository(
    private val source: JsonSource,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    },
) {
    private val cache = mutableMapOf<String, CampusMap>()

    @Synchronized
    fun load(mapId: String): MapLoadResult {
        cache[mapId]?.let { return MapLoadResult.Loaded(it) }

        val base = "maps/$mapId"
        val grid = runCatching {
            json.decodeFromString<MapGrid>(source.read("$base/${mapId.lowercase()}_grid.json"))
        }.getOrElse { return MapLoadResult.Invalid("grid parse failed: ${it.message}") }

        val vector = runCatching {
            json.decodeFromString<VectorMap>(source.read("$base/${mapId.lowercase()}_map.json"))
        }.getOrElse { return MapLoadResult.Invalid("vector parse failed: ${it.message}") }

        val searchIndex = runCatching {
            json.decodeFromString<SearchIndex>(source.read("$base/${mapId.lowercase()}_search_index.json"))
        }.getOrElse { return MapLoadResult.Invalid("search index parse failed: ${it.message}") }

        val faculty = runCatching {
            json.decodeFromString<FacultyDirectory>(source.read("$base/${mapId.lowercase()}_faculty.json"))
        }.getOrElse { return MapLoadResult.Invalid("faculty parse failed: ${it.message}") }

        validate(grid)?.let { return MapLoadResult.Invalid(it) }

        val campus = CampusMap(grid, vector, searchIndex, faculty)
        cache[mapId] = campus
        return MapLoadResult.Loaded(campus)
    }

    fun clearCache() { synchronized(this) { cache.clear() } }

    /**
     * Spec-mandated validation: rows/cols match cell matrix, every POI's approachCell is
     * walkable, both anchors exist and sit on real cells, and anchors land on walkable cells.
     * Returns null on success, or an error message describing the first problem found.
     */
    private fun validate(grid: MapGrid): String? {
        if (grid.cells.size != grid.rows) {
            return "rows mismatch: header says ${grid.rows}, cells has ${grid.cells.size}"
        }
        grid.cells.forEachIndexed { idx, row ->
            if (row.length != grid.cols) {
                return "cols mismatch at row $idx: expected ${grid.cols}, got ${row.length}"
            }
        }
        if (grid.anchors.isEmpty()) return "no anchors defined"
        grid.anchors.forEach { a ->
            if (!grid.inBounds(a.cell.y, a.cell.x)) {
                return "anchor ${a.anchorId} cell ${a.cell} is outside the ${grid.rows}x${grid.cols} grid"
            }
            if (!grid.isWalkable(a.cell.y, a.cell.x)) {
                return "anchor ${a.anchorId} cell ${a.cell} is not walkable (char='${grid.cellAt(a.cell.y, a.cell.x)}')"
            }
        }
        grid.pois.forEach { p ->
            if (!grid.inBounds(p.approachCell.y, p.approachCell.x)) {
                return "poi ${p.id} approachCell ${p.approachCell} is outside the grid"
            }
            if (!grid.isWalkable(p.approachCell.y, p.approachCell.x)) {
                return "poi ${p.id} approachCell is not walkable " +
                        "(char='${grid.cellAt(p.approachCell.y, p.approachCell.x)}')"
            }
        }
        return null
    }
}
