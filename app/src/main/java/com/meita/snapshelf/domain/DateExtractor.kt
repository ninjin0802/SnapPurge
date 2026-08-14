package com.meita.snapshelf.domain

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateExtractor {
    private val isoDate = Regex("\\b(20\\d{2})[-/.](\\d{1,2})[-/.](\\d{1,2})\\b")
    private val japaneseDate = Regex("(\\d{1,2})\\u6708(\\d{1,2})\\u65e5")
    private val slashDate = Regex("\\b(\\d{1,2})/(\\d{1,2})\\b")

    fun extractFirstDeadlineMillis(text: String, now: LocalDate = LocalDate.now()): Long? {
        isoDate.find(text)?.let {
            return parseDate(
                "${it.groupValues[1]}-${it.groupValues[2].padStart(2, '0')}-${it.groupValues[3].padStart(2, '0')}"
            )
        }

        val match = japaneseDate.find(text) ?: slashDate.find(text) ?: return null
        val month = match.groupValues[1].toIntOrNull() ?: return null
        val day = match.groupValues[2].toIntOrNull() ?: return null
        val candidate = runCatching { LocalDate.of(now.year, month, day) }.getOrNull() ?: return null
        val resolved = if (candidate.isBefore(now)) candidate.plusYears(1) else candidate
        return resolved.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun parseDate(value: String): Long? = try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }
}
