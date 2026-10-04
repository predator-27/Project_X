package com.projectx.app.navmap.loader

/**
 * Supplies the raw JSON text for one map bundle, by relative path.
 * The pure-Kotlin loader depends on this; the Android side implements it with
 * `AssetManager.open(...)`.
 */
interface JsonSource {
    /** @throws java.io.IOException if the resource does not exist or cannot be read. */
    fun read(relativePath: String): String
}
