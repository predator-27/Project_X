package com.projectx.app.map

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads the Block N1 map bundle from `assets/maps/`.
 * Caches in memory — parse cost is ~20 ms; keeping it around means
 * the map screen re-opens instantly.
 */
class MapRepository(private val context: Context) {

    @Volatile private var cached: CampusMap? = null

    suspend fun blockN1(): CampusMap = cached ?: withContext(Dispatchers.IO) {
        val vec  = parseVector(readAsset("maps/n1_map.json"))
        val grid = parseGrid  (readAsset("maps/n1_grid.json"))
        val fac  = parseFaculty(readAsset("maps/n1_faculty.json"))
        CampusMap(vec, grid, fac).also { cached = it }
    }

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }

    // ─── Vector ─────────────────────────────────────────────
    private fun parseVector(text: String): MapVector {
        val j = JSONObject(text)
        val units = j.getJSONObject("units")
        return MapVector(
            id     = j.getString("id"),
            name   = j.getString("name"),
            floor  = j.getInt("floor"),
            widthUnits    = units.getDouble("width").toFloat(),
            heightUnits   = units.getDouble("height").toFloat(),
            metersPerUnit = units.getDouble("metersPerUnit").toFloat(),
            widthMeters   = j.getDouble("widthMeters").toFloat(),
            heightMeters  = j.getDouble("heightMeters").toFloat(),
            doors      = j.getJSONArray("doors").map { o ->
                Door(o.getString("kind"), o.getString("label"), o.d("x"), o.d("y"))
            },
            labels     = j.getJSONArray("labels").map { o ->
                VectorLabel(o.getString("text"), o.d("x"), o.d("y"), o.optBoolean("vertical", false))
            },
            corridors  = j.getJSONArray("corridors").map { o ->
                Corridor(
                    id = o.getString("id"), label = o.getString("label"),
                    orientation = o.getString("orientation"),
                    x = o.d("x"), fromY = o.d("fromY"), toY = o.d("toY"),
                )
            },
            aisles     = j.getJSONArray("aisles").map { o ->
                Aisle(o.getInt("n"), o.d("y"), o.getString("label"))
            },
            seats      = j.getJSONArray("seats").map { o -> parseSeat(o) },
            seatCounts = j.getJSONObject("seatCounts").let { sc ->
                SeatCounts(sc.getInt("total"), sc.getInt("cub"), sc.getInt("cab"), sc.getInt("ws"))
            },
        )
    }

    private fun parseSeat(o: JSONObject) = Seat(
        id = o.getString("id"),
        type = o.getString("type"),
        kind = o.getString("kind"),
        num = o.getInt("num"),
        label = o.getString("label"),
        badge = o.getString("badge"),
        zone = o.getString("zone"),
        x = o.d("x"), y = o.d("y"),
        w = o.d("w"), h = o.d("h"),
        cx = o.d("cx"), cy = o.d("cy"),
        fromCorridor = o.optString("fromCorridor").ifBlank { null },
    )

    // ─── Grid ───────────────────────────────────────────────
    private fun parseGrid(text: String): MapGrid {
        val j = JSONObject(text)
        val cellsArr = j.getJSONArray("cells")
        val rows = j.getInt("rows")
        val cells = (0 until cellsArr.length()).map { cellsArr.getString(it) }
        val cellSizeMeters = j.optDouble("cellSizeMeters",
            j.getDouble("cellSizeUnits") * j.getDouble("metersPerUnit")).toFloat()

        return MapGrid(
            mapId = j.getString("mapId"),
            rows = rows,
            cols = j.getInt("cols"),
            cellSizeMeters = cellSizeMeters,
            cellSizeUnits = j.getDouble("cellSizeUnits").toFloat(),
            cells = cells,
            anchors = j.getJSONArray("anchors").map { o ->
                val c = o.getJSONArray("cell")
                Anchor(
                    anchorId = o.getString("anchorId"),
                    label = o.getString("label"),
                    cellRow = c.getInt(0), cellCol = c.getInt(1),
                    headingDeg = o.getInt("headingDeg"),
                )
            },
            pois = j.getJSONArray("pois").map { o ->
                val c = o.getJSONArray("cell")
                val a = o.getJSONArray("approachCell")
                Poi(
                    id = o.getString("id"),
                    type = o.getString("type"),
                    label = o.getString("label"),
                    badge = o.getString("badge"),
                    zone = o.getString("zone"),
                    cellRow = c.getInt(0), cellCol = c.getInt(1),
                    approachRow = a.getInt(0), approachCol = a.getInt(1),
                    tags = o.optJSONArray("tags")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList(),
                )
            },
            walkableCells = j.optJSONObject("verified")?.optInt("walkableCells", 0) ?: 0,
        )
    }

    // ─── Faculty ────────────────────────────────────────────
    private fun parseFaculty(text: String): List<Faculty> {
        val arr = JSONObject(text).getJSONArray("faculty")
        return arr.map { o ->
            Faculty(
                seatId = o.getString("seat"),
                name = o.getString("name"),
                designation = o.getString("designation"),
                department = o.getString("department"),
                subjects = o.optJSONArray("subjects")?.let { s ->
                    (0 until s.length()).map { s.getString(it) }
                } ?: emptyList(),
                status = o.optString("status", "Unknown"),
                hours = o.optString("hours", ""),
                email = o.optString("email", ""),
            )
        }
    }

    // ─── org.json ergonomics ────────────────────────────────
    private inline fun <R> JSONArray.map(transform: (JSONObject) -> R): List<R> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private fun JSONObject.d(key: String): Float = getDouble(key).toFloat()
}
