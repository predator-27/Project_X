package com.projectx.app.navmap.loader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * Loads the actual N1 bundle shipped under `app/src/main/assets/maps/N1/` straight off
 * disk (tests run from the module root), so this proves the real files parse and pass
 * validation together with the models and the loader.
 */
class MapRepositoryTest {

    private val assetsRoot = File("src/main/assets")

    private val fileSystemSource = object : JsonSource {
        override fun read(relativePath: String): String =
            File(assetsRoot, relativePath).readText(Charsets.UTF_8)
    }

    @Test
    fun bundled_N1_parses_and_passes_validation() {
        require(assetsRoot.exists()) {
            "expected assets at ${assetsRoot.absolutePath} — run tests from the :app module dir"
        }
        val result = MapRepository(fileSystemSource).load("N1")
        when (result) {
            is MapLoadResult.Loaded -> {
                val campus = result.map
                assertEquals("N1", campus.grid.mapId)
                assertEquals("N1", campus.vector.id)
                assertEquals("N1", campus.searchIndex.mapId)
                assertEquals("N1", campus.faculty.mapId)

                // Known counts from the shipped bundle.
                assertEquals(103, campus.grid.rows)
                assertEquals(91, campus.grid.cols)
                assertEquals(119, campus.grid.pois.size)
                assertEquals(2, campus.grid.anchors.size)
                assertEquals(12, campus.faculty.faculty.size)

                // Entrance + exit are both there.
                assertNotNull(campus.grid.anchorById["N1-A01"])
                assertNotNull(campus.grid.anchorById["N1-A02"])

                // Routing rule: walkable chars include path and door.
                assertTrue('.' in campus.grid.routing.walkableSet)
                assertTrue('D' in campus.grid.routing.walkableSet)
            }
            is MapLoadResult.Invalid -> fail("expected Loaded, got Invalid: ${result.reason}")
        }
    }

    @Test
    fun every_poi_approach_cell_is_walkable() {
        val campus = (MapRepository(fileSystemSource).load("N1") as MapLoadResult.Loaded).map
        campus.grid.pois.forEach { poi ->
            val ch = campus.grid.cellAt(poi.approachCell.y, poi.approachCell.x)
            assertTrue(
                "poi ${poi.id} approachCell(${poi.approachCell}) is char '$ch', not walkable",
                campus.grid.routing.isWalkable(ch),
            )
        }
    }

    @Test
    fun rows_mismatch_is_reported_as_Invalid() {
        val badGrid = File(assetsRoot, "maps/N1/n1_grid.json").readText()
            .replace("\"rows\": 103", "\"rows\": 102")
        val src = object : JsonSource {
            override fun read(relativePath: String): String = when {
                relativePath.endsWith("n1_grid.json") -> badGrid
                else -> fileSystemSource.read(relativePath)
            }
        }
        val result = MapRepository(src).load("N1")
        assertTrue(
            "expected Invalid, got $result",
            result is MapLoadResult.Invalid && result.reason.contains("rows mismatch"),
        )
    }

    @Test
    fun unknown_fields_are_ignored() {
        val withExtra = File(assetsRoot, "maps/N1/n1_grid.json").readText()
            .replace("\"schemaVersion\": 2,", "\"schemaVersion\": 2, \"somethingNew\": \"hello\",")
        val src = object : JsonSource {
            override fun read(relativePath: String): String = when {
                relativePath.endsWith("n1_grid.json") -> withExtra
                else -> fileSystemSource.read(relativePath)
            }
        }
        val result = MapRepository(src).load("N1")
        assertTrue("expected Loaded, got $result", result is MapLoadResult.Loaded)
    }
}
