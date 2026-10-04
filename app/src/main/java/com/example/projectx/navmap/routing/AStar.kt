package com.projectx.app.navmap.routing

import com.projectx.app.navmap.model.MapGrid
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** (row, col) step on the grid. */
data class GridNode(val r: Int, val c: Int)

data class AStarResult(
    val path: List<GridNode>,
    val cost: Double,
) {
    val found: Boolean get() = path.isNotEmpty()
    companion object {
        val Empty = AStarResult(emptyList(), 0.0)
    }
}

/**
 * A* on the campus grid. 8-directional moves. Diagonals cost √2.
 * Corner-cutting forbidden: a diagonal (r,c) -> (r+dr, c+dc) requires both
 * (r, c+dc) and (r+dr, c) to be walkable. Returns an empty path on no route.
 */
object AStar {
    private val DIRS = intArrayOf(
        -1, 0,  1, 0,  0, -1,  0, 1,         // orthogonal
        -1, -1, -1, 1, 1, -1,  1, 1,         // diagonal
    )
    private val SQRT2 = sqrt(2.0)

    fun find(grid: MapGrid, start: GridNode, goal: GridNode): AStarResult {
        if (!grid.isWalkable(start.r, start.c)) return AStarResult.Empty
        if (!grid.isWalkable(goal.r, goal.c)) return AStarResult.Empty
        if (start == goal) return AStarResult(listOf(start), 0.0)

        val rows = grid.rows
        val cols = grid.cols
        val closed = BooleanArray(rows * cols)
        val gScore = DoubleArray(rows * cols) { Double.POSITIVE_INFINITY }
        val cameFrom = IntArray(rows * cols) { -1 }
        fun idx(r: Int, c: Int) = r * cols + c

        val startIdx = idx(start.r, start.c)
        gScore[startIdx] = 0.0

        // (fScore, node-index)
        val open = PriorityQueue<DoubleArray>(compareBy { it[0] })
        open.add(doubleArrayOf(heuristic(start, goal), startIdx.toDouble()))

        while (open.isNotEmpty()) {
            val top = open.poll()
            val currentIdx = top[1].toInt()
            if (closed[currentIdx]) continue
            closed[currentIdx] = true

            val cr = currentIdx / cols
            val cc = currentIdx % cols
            if (cr == goal.r && cc == goal.c) {
                return AStarResult(reconstruct(cameFrom, startIdx, currentIdx, cols), gScore[currentIdx])
            }

            var i = 0
            while (i < DIRS.size) {
                val dr = DIRS[i]
                val dc = DIRS[i + 1]
                i += 2
                val nr = cr + dr
                val nc = cc + dc
                if (nr !in 0 until rows || nc !in 0 until cols) continue
                if (!grid.isWalkable(nr, nc)) continue
                val diagonal = dr != 0 && dc != 0
                if (diagonal) {
                    // Corner-cutting guard.
                    if (!grid.isWalkable(cr, nc) || !grid.isWalkable(nr, cc)) continue
                }
                val nIdx = idx(nr, nc)
                if (closed[nIdx]) continue
                val stepCost = if (diagonal) SQRT2 else 1.0
                val tentative = gScore[currentIdx] + stepCost
                if (tentative < gScore[nIdx]) {
                    gScore[nIdx] = tentative
                    cameFrom[nIdx] = currentIdx
                    val f = tentative + heuristic(GridNode(nr, nc), goal)
                    open.add(doubleArrayOf(f, nIdx.toDouble()))
                }
            }
        }
        return AStarResult.Empty
    }

    /** Octile distance — admissible for 8-dir movement with sqrt(2) diagonals. */
    private fun heuristic(a: GridNode, b: GridNode): Double {
        val dr = abs(a.r - b.r).toDouble()
        val dc = abs(a.c - b.c).toDouble()
        val lo = min(dr, dc)
        val hi = max(dr, dc)
        return (SQRT2 - 1.0) * lo + hi
    }

    private fun reconstruct(cameFrom: IntArray, startIdx: Int, endIdx: Int, cols: Int): List<GridNode> {
        val out = ArrayDeque<GridNode>()
        var cur = endIdx
        while (cur != -1) {
            out.addFirst(GridNode(cur / cols, cur % cols))
            if (cur == startIdx) break
            cur = cameFrom[cur]
        }
        return out.toList()
    }
}
