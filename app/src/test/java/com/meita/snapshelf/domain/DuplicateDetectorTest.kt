package com.meita.snapshelf.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DuplicateDetectorTest {
    @Test
    fun returnsGroupForNearHash() {
        val known = listOf(1L to "11110000")
        assertEquals("dup-1", DuplicateDetector.duplicateGroupFor("11110001", known, threshold = 1))
    }

    @Test
    fun ignoresFarHash() {
        val known = listOf(1L to "11110000")
        assertNull(DuplicateDetector.duplicateGroupFor("00001111", known, threshold = 1))
    }
}

