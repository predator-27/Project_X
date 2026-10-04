package com.projectx.app.navmap.position

/**
 * User step length in metres. Defaults to the common Weinberg approximation
 * `0.415 * height`. [refineFromScans] updates it when two consecutive anchor scans
 * give us a true walked distance and a step count.
 */
class StepLength(initialMeters: Double) {
    private var length: Double = initialMeters

    fun current(): Double = length

    /** @return the new step length. */
    fun refineFromScans(walkedMeters: Double, stepsBetween: Int): Double {
        if (stepsBetween <= 0 || walkedMeters <= 0.0) return length
        val measured = walkedMeters / stepsBetween
        // Smooth: 70% old, 30% new, to resist a single bad reading.
        length = 0.7 * length + 0.3 * measured
        return length
    }

    companion object {
        fun fromHeight(userHeightMeters: Double): StepLength =
            StepLength(0.415 * userHeightMeters)
    }
}
