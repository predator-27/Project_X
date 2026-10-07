package com.projectx.app

import com.projectx.app.update.UpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SemVerComparisonTest {

    private val updateManager = UpdateManager.getInstance()

    @Test
    fun testVersionHigherWithMajorRelease() {
        assertTrue(updateManager.isVersionHigher("v2.0.0", "1.9.9"))
        assertTrue(updateManager.isVersionHigher("2.0", "1.8.0"))
    }

    @Test
    fun testVersionHigherWithMinorRelease() {
        assertTrue(updateManager.isVersionHigher("1.10.0", "1.9.0"))
        assertTrue(updateManager.isVersionHigher("1.10", "1.9"))
        assertTrue(updateManager.isVersionHigher("v1.8.1", "1.8.0"))
    }

    @Test
    fun testVersionHigherWithSameVersion() {
        assertFalse(updateManager.isVersionHigher("1.8.0", "1.8.0"))
        assertFalse(updateManager.isVersionHigher("v1.8.0", "1.8.0"))
        assertFalse(updateManager.isVersionHigher("1.8", "1.8.0"))
    }

    @Test
    fun testVersionHigherWithOlderRelease() {
        assertFalse(updateManager.isVersionHigher("1.7.9", "1.8.0"))
        assertFalse(updateManager.isVersionHigher("v1.0.0", "1.8.0"))
    }

    @Test
    fun testVersionHigherWithPrefixedStrings() {
        assertTrue(updateManager.isVersionHigher("release-1.9.0", "1.8.0"))
        assertTrue(updateManager.isVersionHigher("release/2.1.0", "v2.0.0"))
    }
}
