package com.projectx.app.navmap.loader

import android.content.Context

/**
 * The one Android-touching class in `navmap/loader/`. Hands [MapRepository] the raw
 * JSON text from the APK's `assets/` folder.
 */
class AssetJsonSource(context: Context) : JsonSource {
    private val assets = context.applicationContext.assets

    override fun read(relativePath: String): String =
        assets.open(relativePath).bufferedReader(Charsets.UTF_8).use { it.readText() }
}
