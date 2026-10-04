package com.projectx.app.navmap.position

import com.projectx.app.navmap.model.MapGrid
import com.projectx.app.navmap.model.PositionEstimate
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Map-matching particle filter. ~300 particles, resampled every step. Any particle
 * entering a non-walkable cell is killed. Reports the weighted mean as position and
 * the particle spread as accuracy in metres.
 */
class ParticleFilter(
    private val grid: MapGrid,
    private val count: Int = 300,
    private val headingNoiseDeg: Double = 6.0,
    private val stepNoiseFrac: Double = 0.10,
    private val random: Random = Random.Default,
) {
    private data class Particle(var r: Double, var c: Double, var w: Double)

    private val particles = ArrayList<Particle>(count)

    fun seed(startRow: Double, startCol: Double, spreadCells: Double = 0.0) {
        particles.clear()
        repeat(count) {
            particles.add(
                Particle(
                    r = startRow + random.gaussian() * spreadCells,
                    c = startCol + random.gaussian() * spreadCells,
                    w = 1.0 / count,
                ),
            )
        }
    }

    /** Advance every particle by one noisy step, then kill particles that hit a wall. */
    fun step(stepCells: Double, headingDeg: Double) {
        if (particles.isEmpty()) return
        val headingRad = Math.toRadians(headingDeg)
        val noisyStep = { stepCells * (1.0 + random.gaussian() * stepNoiseFrac) }
        val noisyHeading = { headingRad + Math.toRadians(random.gaussian() * headingNoiseDeg) }
        for (p in particles) {
            val step = noisyStep()
            val h = noisyHeading()
            // Compass convention: 0 = north = -row, 90 = east = +col.
            val nr = p.r - step * cos(h)
            val nc = p.c + step * sin(h)
            if (grid.isWalkable(nr.toInt(), nc.toInt())) {
                p.r = nr; p.c = nc
            } else {
                p.w = 0.0
            }
        }
        normaliseAndResample()
    }

    fun estimate(): PositionEstimate {
        if (particles.isEmpty()) return PositionEstimate(0.0, 0.0, 0.0, Double.POSITIVE_INFINITY)
        var sumR = 0.0
        var sumC = 0.0
        var sumW = 0.0
        for (p in particles) { sumR += p.r * p.w; sumC += p.c * p.w; sumW += p.w }
        if (sumW == 0.0) return PositionEstimate(0.0, 0.0, 0.0, Double.POSITIVE_INFINITY)
        val meanR = sumR / sumW
        val meanC = sumC / sumW
        var variance = 0.0
        for (p in particles) {
            val dr = p.r - meanR
            val dc = p.c - meanC
            variance += p.w * (dr * dr + dc * dc)
        }
        val spreadCells = sqrt(variance / sumW)
        return PositionEstimate(
            cellRow = meanR,
            cellCol = meanC,
            headingDeg = 0.0,
            accuracyMeters = spreadCells * grid.routing.cellMeters,
        )
    }

    private fun normaliseAndResample() {
        val total = particles.sumOf { it.w }
        if (total <= 0.0) return
        for (p in particles) p.w /= total

        val n = particles.size
        val step = 1.0 / n
        var threshold = random.nextDouble() * step
        var cumulative = 0.0
        var i = 0
        val resampled = ArrayList<Particle>(n)
        for (p in particles) {
            cumulative += p.w
            while (threshold < cumulative && resampled.size < n) {
                resampled.add(Particle(p.r, p.c, 1.0 / n))
                threshold += step
                i++
            }
        }
        while (resampled.size < n) resampled.add(Particle(particles.last().r, particles.last().c, 1.0 / n))
        particles.clear()
        particles.addAll(resampled)
    }

    private fun Random.gaussian(): Double {
        val u1 = nextDouble()
        val u2 = nextDouble()
        return hypot(0.0, 1.0) + sqrt(-2.0 * kotlin.math.ln(u1.coerceAtLeast(1e-9))) * cos(2 * Math.PI * u2)
    }
}
