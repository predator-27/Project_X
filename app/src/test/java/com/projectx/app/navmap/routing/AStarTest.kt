package com.projectx.app.navmap.routing

import com.projectx.app.navmap.loader.JsonSource
import com.projectx.app.navmap.loader.MapLoadResult
import com.projectx.app.navmap.loader.MapRepository
import com.projectx.app.navmap.model.CampusMap
import com.projectx.app.navmap.model.Cell
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AStarTest {

    private val campus: CampusMap by lazy {
        val root = File("src/main/assets")
        val src = object : JsonSource { override fun read(p: String) = File(root, p).readText() }
        (MapRepository(src).load("N1") as MapLoadResult.Loaded).map
    }

    @Test
    fun entrance_to_ws52_route_exists_and_matches_bundled_distance() {
        val entrance = campus.grid.anchorById["N1-A02"]!!
        val target = campus.grid.poiById["N1-WS-52"]!!
        val result = AStar.find(
            campus.grid,
            GridNode(entrance.cell.y, entrance.cell.x),
            GridNode(target.approachCell.y, target.approachCell.x),
        )
        assertTrue("expected a route from entrance to WS-52", result.found)
        val distanceM = result.cost * campus.grid.routing.cellMeters
        val expected = target.distanceFromEntranceM!!
        assertEquals("WS-52 distance should match bundled distanceFromEntranceM", expected, distanceM, 0.5)
    }

    @Test
    fun entrance_to_exit_route_exists() {
        val entrance = campus.grid.anchorById["N1-A02"]!!
        val exit = campus.grid.anchorById["N1-A01"]!!
        val result = AStar.find(
            campus.grid,
            GridNode(entrance.cell.y, entrance.cell.x),
            GridNode(exit.cell.y, exit.cell.x),
        )
        assertTrue("expected a route from entrance to exit", result.found)
        assertTrue("exit route should have multiple cells", result.path.size > 10)
    }

    @Test
    fun unreachable_wall_cell_returns_empty() {
        // (0,0) is a '#' per the bundle.
        val entrance = campus.grid.anchorById["N1-A02"]!!
        val result = AStar.find(
            campus.grid,
            GridNode(entrance.cell.y, entrance.cell.x),
            GridNode(0, 0),
        )
        assertFalse("should not find a route into a wall", result.found)
        assertEquals(0, result.path.size)
    }

    @Test
    fun smoothing_keeps_endpoints_and_preserves_walkability() {
        val entrance = campus.grid.anchorById["N1-A02"]!!
        val target = campus.grid.poiById["N1-WS-52"]!!
        val raw = AStar.find(
            campus.grid,
            GridNode(entrance.cell.y, entrance.cell.x),
            GridNode(target.approachCell.y, target.approachCell.x),
        ).path
        val smoothed = PathSmoother.smooth(campus.grid, raw)
        assertEquals(raw.first(), smoothed.first())
        assertEquals(raw.last(), smoothed.last())
        assertTrue("smoothing should not grow the path", smoothed.size <= raw.size)
        smoothed.forEach { n ->
            assertTrue("smoothed node must be walkable", campus.grid.isWalkable(n.r, n.c))
        }
    }

    @Test
    fun route_builder_distance_matches_astar_cost() {
        val entrance = campus.grid.anchorById["N1-A02"]!!
        val route = RouteBuilder.toPoi(campus, Cell(entrance.cell.x, entrance.cell.y), "N1-WS-52")
        assertTrue("route found", route.found)
        val expected = campus.grid.poiById["N1-WS-52"]!!.distanceFromEntranceM!!
        assertEquals("RouteBuilder distance should match bundled", expected, route.distanceMeters, 0.5)
        assertTrue("should have turn-by-turn steps", route.steps.isNotEmpty())
    }
}
