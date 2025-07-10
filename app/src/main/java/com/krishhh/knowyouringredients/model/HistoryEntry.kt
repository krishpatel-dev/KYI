package com.krishhh.knowyouringredients.model

data class HistoryEntry(
    val phrase: String,
    val timestamp: Long = System.currentTimeMillis()
)
