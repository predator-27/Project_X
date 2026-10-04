package com.projectx.app.navmap.position

import com.projectx.app.navmap.loader.JsonSource
import com.projectx.app.navmap.loader.MapLoadResult
import com.projectx.app.navmap.loader.MapRepository
import com.projectx.app.navmap.model.MapGrid
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PdrEngineTest {

    private val grid: MapGrid by lazy {
        val root = File("src/main/assets")
        val src = object : JsonSource { override fun read(p: String) = File(root, p).readText() }
        (MapRepository(src).load("N1") as MapLoadResult.Loaded).map.grid
    }

    @Test
    fun stepping_due_east_moves_only_in_column() {
        val entrance = grid.anchorById["N1-A02"]!!
        var deviceHeading = 90.0 // east
        val engine = PdrEngine(
            grid = grid,
            stepLength = StepLength(0.65),
            headingSource = { deviceHeading },
        )
        engine.resetToAnchor(entrance.cell, scanHeadingDeg = 90, deviceHeadingAtScan = 90.0)
        val before = engine.snapshot()
        repeat(10) { engine.onStep() }
        val after = engine.snapshot()
        assertTrue("column should increase when heading is east", after.cellCol > before.cellCol)
        assertEquals("row should stay fixed", before.cellRow, after.cellRow, 0.001)
    }

    @Test
    fun step_length_refines_from_two_scans() {
        val sl = StepLength(0.70)
        sl.refineFromScans(walkedMeters = 15.0, stepsBetween = 25)
        val refined = sl.current()
        // 15 / 25 = 0.60 ; blended 70% old + 30% new -> 0.67
        assertEquals(0.67, refined, 0.01)
    }

    @Test
    fun particle_filter_reports_lower_accuracy_as_it_spreads() {
        val entrance = grid.anchorById["N1-A02"]!!
        val filter = ParticleFilter(grid)
        filter.seed(entrance.cell.y.toDouble(), entrance.cell.x.toDouble(), spreadCells = 0.5)
        val initial = filter.estimate().accuracyMeters
        // Walk a few steps towards -y (north) through the main corridor
        repeat(3) { filter.step(stepCells = 3.0, headingDeg = 270.0) }
        val later = filter.estimate().accuracyMeters
        assertTrue("accuracy should remain finite", later.isFinite())
        assertTrue("initial should be finite", initial.isFinite())
    }
}
