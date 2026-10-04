package com.projectx.app.navmap.routing

import com.projectx.app.navmap.model.MapGrid
import kotlin.math.abs
import kotlin.math.max

/**
 * Line-of-sight simplification: keep a waypoint only when the straight line to the
 * next waypoint crosses a non-walkable cell. Result has long straight runs instead
 * of stair-steps, so the drawn path looks clean. Keep the raw path for distance.
 */
object PathSmoother {
    fun smooth(grid: MapGrid, path: List<GridNode>): List<GridNode> {
        if (path.size <= 2) return path
        val out = mutableListOf<GridNode>()
        out.add(path.first())
        var anchor = 0
        var i = 1
        while (i < path.size - 1) {
            if (!hasLineOfSight(grid, path[anchor], path[i + 1])) {
                out.add(path[i])
                anchor = i
            }
            i++
        }
        out.add(path.last())
        return out
    }

    /** Walk the grid cells crossed by the segment from `a` to `b`; return true if every cell is walkable. */
    fun hasLineOfSight(grid: MapGrid, a: GridNode, b: GridNode): Boolean {
        val dr = b.r - a.r
        val dc = b.c - a.c
        val steps = max(abs(dr), abs(dc))
        if (steps == 0) return grid.isWalkable(a.r, a.c)
        for (s in 0..steps) {
            val t = s.toDouble() / steps
            val r = (a.r + t * dr).let { kotlin.math.round(it).toInt() }
            val c = (a.c + t * dc).let { kotlin.math.round(it).toInt() }
            if (!grid.isWalkable(r, c)) return false
        }
        return true
    }
}
