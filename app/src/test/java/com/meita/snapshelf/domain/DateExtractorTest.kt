package com.meita.snapshelf.domain

import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class DateExtractorTest {
    @Test
    fun extractsIsoDate() {
        val millis = DateExtractor.extractFirstDeadlineMillis("期限: 2026-09-20")
        assertNotNull(millis)
    }

    @Test
    fun extractsJapaneseMonthDay() {
        val millis = DateExtractor.extractFirstDeadlineMillis("締切は9月10日です", LocalDate.of(2026, 8, 14))
        assertNotNull(millis)
    }
}

