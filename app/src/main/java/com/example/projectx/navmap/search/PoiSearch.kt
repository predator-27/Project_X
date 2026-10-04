package com.projectx.app.navmap.search

import com.projectx.app.navmap.model.SearchEntry
import com.projectx.app.navmap.model.SearchIndex

/**
 * Query-time ranking over the prebuilt search index.
 *
 * Rank by match quality:
 *  1. exact id or label equals query
 *  2. any term starts with the query
 *  3. any term contains the query
 * then break ties by `distanceFromEntranceM` ascending (the field in the index — not the
 * live route distance from the user, which the caller recomputes with A*).
 */
object PoiSearch {

    private val DIACRITIC = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    private val PUNCT = "[^\\p{L}\\p{Nd}\\s]+".toRegex()
    private val SPACES = "\\s+".toRegex()

    fun normalise(input: String): String {
        if (input.isEmpty()) return ""
        val decomposed = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
        return DIACRITIC.replace(decomposed, "")
            .lowercase()
            .let { PUNCT.replace(it, " ") }
            .let { SPACES.replace(it, " ") }
            .trim()
    }

    data class Hit(val entry: SearchEntry, val quality: Int)

    fun query(index: SearchIndex, raw: String, limit: Int = 20): List<SearchEntry> {
        val q = normalise(raw)
        if (q.isEmpty()) return emptyList()
        val hits = ArrayList<Hit>()
        for (e in index.entries) {
            val quality = scoreEntry(e, q)
            if (quality > 0) hits.add(Hit(e, quality))
        }
        hits.sortWith(
            compareByDescending<Hit> { it.quality }
                .thenBy { it.entry.distanceFromEntranceM ?: Double.POSITIVE_INFINITY },
        )
        return hits.take(limit).map { it.entry }
    }

    private fun scoreEntry(entry: SearchEntry, q: String): Int {
        val id = normalise(entry.id)
        val label = normalise(entry.label)
        if (id == q || label == q) return 100
        val terms = entry.terms.map { normalise(it) }
        if (terms.any { it == q }) return 90
        if (terms.any { it.startsWith(q) }) return 60
        if (terms.any { it.contains(q) }) return 30
        if (label.startsWith(q)) return 55
        if (label.contains(q)) return 25
        val person = entry.person ?: return 0
        if (normalise(person.name).contains(q)) return 20
        if (person.subjects.any { normalise(it).contains(q) }) return 15
        if (person.department?.let { normalise(it).contains(q) } == true) return 10
        return 0
    }
}
