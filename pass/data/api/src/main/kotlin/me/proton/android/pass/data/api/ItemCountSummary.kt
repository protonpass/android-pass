package me.proton.android.pass.data.api

data class ItemCountSummary(
    val total: Long,
    val login: Long,
    val note: Long,
    val alias: Long
) {
    companion object {
        val Initial = ItemCountSummary(
            total = 0,
            login = 0,
            note = 0,
            alias = 0
        )
    }
}
