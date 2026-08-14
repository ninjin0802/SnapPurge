package com.meita.snapshelf.domain

object DuplicateDetector {
    fun hammingDistance(left: String, right: String): Int {
        if (left.length != right.length) return Int.MAX_VALUE
        return left.zip(right).count { (a, b) -> a != b }
    }

    fun duplicateGroupFor(hash: String, known: List<Pair<Long, String>>, threshold: Int = 6): String? {
        val match = known.firstOrNull { (_, otherHash) -> hammingDistance(hash, otherHash) <= threshold }
        return match?.let { "dup-${it.first}" }
    }
}

