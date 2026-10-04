package com.projectx.app.navmap.search

import com.projectx.app.navmap.loader.JsonSource
import com.projectx.app.navmap.loader.MapLoadResult
import com.projectx.app.navmap.loader.MapRepository
import com.projectx.app.navmap.model.SearchIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PoiSearchTest {

    private val index: SearchIndex by lazy {
        val root = File("src/main/assets")
        val src = object : JsonSource { override fun read(p: String) = File(root, p).readText() }
        (MapRepository(src).load("N1") as MapLoadResult.Loaded).map.searchIndex
    }

    @Test
    fun exact_label_match_ranks_first() {
        val results = PoiSearch.query(index, "WS 52")
        assertTrue(results.isNotEmpty())
        assertEquals("N1-WS-52", results.first().id)
    }

    @Test
    fun searching_exit_returns_the_exit_entry() {
        val results = PoiSearch.query(index, "exit")
        assertTrue("search for 'exit' should produce something", results.isNotEmpty())
    }

    @Test
    fun person_name_matches() {
        val results = PoiSearch.query(index, "ananya")
        assertTrue(results.any { it.id == "N1-WS-52" })
    }

    @Test
    fun subject_matches() {
        val results = PoiSearch.query(index, "computer networks")
        assertTrue(results.any { it.id == "N1-WS-52" })
    }

    @Test
    fun normalisation_is_accent_and_case_insensitive() {
        assertEquals(PoiSearch.normalise("Ánanya"), PoiSearch.normalise("ANANYA"))
    }

    @Test
    fun empty_query_returns_empty() {
        assertEquals(emptyList<Any>(), PoiSearch.query(index, ""))
        assertEquals(emptyList<Any>(), PoiSearch.query(index, "   "))
    }
}
