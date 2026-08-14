package com.meita.snapshelf.domain

object SummaryExtractor {
    fun summarize(text: String): String {
        val normalized = text.replace(Regex("\\s+"), " ").trim()
        if (normalized.isBlank()) return "OCRテキストがまだありません。"

        val sentence = normalized
            .split(Regex("(?<=[。.!?？])\\s*"))
            .firstOrNull { it.length >= 12 }
            ?: normalized
        return sentence.take(120)
    }
}

