package com.meita.snapshelf.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryClassifierTest {
    @Test
    fun classifiesShoppingText() {
        val category = CategoryClassifier.classify("Amazonの注文が配送されました", "Screenshot.png")
        assertEquals("買い物", category)
    }

    @Test
    fun classifiesScheduleText() {
        val category = CategoryClassifier.classify("明日 10:00 meeting 予定", "Screenshot.png")
        assertEquals("予定", category)
    }
}

