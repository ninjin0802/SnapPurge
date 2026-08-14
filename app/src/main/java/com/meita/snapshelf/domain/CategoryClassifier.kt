package com.meita.snapshelf.domain

object CategoryClassifier {
    private val rules = listOf(
        "買い物" to listOf("order", "cart", "receipt", "payment", "amazon", "楽天", "注文", "配送", "購入", "領収"),
        "予定" to listOf("calendar", "meeting", "zoom", "予約", "予定", "締切", "期限", "明日", "今日", "pm", "am"),
        "仕事" to listOf("slack", "github", "linear", "asana", "jira", "pull request", "meeting", "仕様", "議事録"),
        "学習" to listOf("lecture", "lesson", "study", "course", "問題", "解説", "単語", "学習", "講座"),
        "SNS" to listOf("x.com", "twitter", "instagram", "threads", "tiktok", "facebook", "投稿", "フォロー"),
        "旅行" to listOf("hotel", "flight", "booking", "airbnb", "新幹線", "航空券", "ホテル", "チェックイン"),
        "金融" to listOf("bank", "card", "invoice", "銀行", "請求", "残高", "振込", "決済")
    )

    fun classify(text: String, displayName: String): String {
        val haystack = "$displayName\n$text".lowercase()
        return rules.firstOrNull { (_, keywords) -> keywords.any { haystack.contains(it.lowercase()) } }?.first
            ?: "メモ"
    }

    fun suggestedTags(text: String): List<String> {
        val words = text
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 3 }
        return words
            .groupingBy { it.lowercase() }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }
}

