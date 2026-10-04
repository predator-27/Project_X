package com.projectx.app.navmap.qr

import com.projectx.app.navmap.model.MapGrid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The payload baked into each printed anchor plate — a compact JSON object. Example:
 * `{"v":1,"map":"N1","a":"N1-A02","x":63,"y":96,"hdg":0}`.
 * `sig` holds an Ed25519 signature over the canonical JSON once plates are signed.
 */
@Serializable
data class QrPayload(
    @SerialName("v") val version: Int,
    @SerialName("map") val mapId: String,
    @SerialName("a") val anchorId: String,
    val x: Int,
    val y: Int,
    @SerialName("hdg") val headingDeg: Int,
    @SerialName("sig") val signature: String? = null,
)

sealed class QrResult {
    data class Valid(val payload: QrPayload) : QrResult()
    data class Invalid(val reason: String) : QrResult()
}

object QrPayloadParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(raw: String?): QrResult {
        if (raw.isNullOrBlank()) return QrResult.Invalid("empty QR payload")
        return runCatching { json.decodeFromString<QrPayload>(raw) }
            .fold(
                onSuccess = { QrResult.Valid(it) },
                onFailure = { QrResult.Invalid("not a valid campus anchor QR payload") },
            )
    }

    /** Payload is only trusted if the anchor is known to the loaded map and sits on a walkable cell. */
    fun verifyAgainst(payload: QrPayload, grid: MapGrid): QrResult {
        if (payload.mapId != grid.mapId) {
            return QrResult.Invalid("QR is for map '${payload.mapId}', loaded map is '${grid.mapId}'")
        }
        val anchor = grid.anchorById[payload.anchorId]
            ?: return QrResult.Invalid("unknown anchor '${payload.anchorId}'")
        if (anchor.cell.x != payload.x || anchor.cell.y != payload.y) {
            return QrResult.Invalid("QR cell (${payload.x},${payload.y}) does not match anchor ${anchor.cell}")
        }
        if (!grid.inBounds(payload.y, payload.x)) {
            return QrResult.Invalid("QR cell is outside the ${grid.rows}x${grid.cols} grid")
        }
        if (!grid.isWalkable(payload.y, payload.x)) {
            return QrResult.Invalid("QR cell is not walkable (char='${grid.cellAt(payload.y, payload.x)}')")
        }
        return QrResult.Valid(payload)
    }
}

/**
 * Signature verification scaffolding. The production payload will carry an Ed25519
 * signature over the canonical JSON; wire a real verifier in later.
 */
interface SignatureVerifier {
    fun verify(payload: QrPayload): Boolean
}

/** Fallback used in debug when `requireSignedAnchors` is false. */
object AllowUnsignedVerifier : SignatureVerifier {
    override fun verify(payload: QrPayload): Boolean = true
}
