package me.proton.android.pass.data.api

data class ItemCountSummary(
    val total: Long,
    val login: Long,
    val note: Long,
    val alias: Long
)
