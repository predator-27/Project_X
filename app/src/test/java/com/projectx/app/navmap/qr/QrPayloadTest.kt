package com.projectx.app.navmap.qr

import com.projectx.app.navmap.loader.JsonSource
import com.projectx.app.navmap.loader.MapLoadResult
import com.projectx.app.navmap.loader.MapRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QrPayloadTest {

    private val grid by lazy {
        val root = File("src/main/assets")
        val src = object : JsonSource { override fun read(p: String) = File(root, p).readText() }
        (MapRepository(src).load("N1") as MapLoadResult.Loaded).map.grid
    }

    @Test
    fun bundled_entrance_payload_round_trips_and_verifies() {
        val raw = grid.anchorById["N1-A02"]!!.payload
        val parsed = QrPayloadParser.parse(raw)
        assertTrue(parsed is QrResult.Valid)
        val payload = (parsed as QrResult.Valid).payload
        assertEquals("N1-A02", payload.anchorId)
        assertEquals("N1", payload.mapId)

        val verified = QrPayloadParser.verifyAgainst(payload, grid)
        assertTrue(verified is QrResult.Valid)
    }

    @Test
    fun bundled_exit_payload_verifies() {
        val raw = grid.anchorById["N1-A01"]!!.payload
        val verified = QrPayloadParser.parse(raw).let {
            require(it is QrResult.Valid)
            QrPayloadParser.verifyAgainst(it.payload, grid)
        }
        assertTrue(verified is QrResult.Valid)
    }

    @Test
    fun unknown_anchor_id_is_rejected() {
        val payload = QrPayload(1, "N1", "N1-A99", 63, 96, 0)
        val result = QrPayloadParser.verifyAgainst(payload, grid)
        assertTrue(result is QrResult.Invalid)
        assertTrue((result as QrResult.Invalid).reason.contains("unknown anchor"))
    }

    @Test
    fun wrong_map_id_is_rejected() {
        val payload = QrPayload(1, "N2", "N1-A02", 63, 96, 0)
        val result = QrPayloadParser.verifyAgainst(payload, grid)
        assertTrue(result is QrResult.Invalid)
    }

    @Test
    fun cell_mismatch_is_rejected() {
        val payload = QrPayload(1, "N1", "N1-A02", 10, 10, 0)
        val result = QrPayloadParser.verifyAgainst(payload, grid)
        assertTrue(result is QrResult.Invalid)
    }

    @Test
    fun garbage_input_is_rejected() {
        assertTrue(QrPayloadParser.parse(null) is QrResult.Invalid)
        assertTrue(QrPayloadParser.parse("") is QrResult.Invalid)
        assertTrue(QrPayloadParser.parse("not json") is QrResult.Invalid)
        assertTrue(QrPayloadParser.parse("{\"not\":\"ours\"}") is QrResult.Invalid)
    }
}
