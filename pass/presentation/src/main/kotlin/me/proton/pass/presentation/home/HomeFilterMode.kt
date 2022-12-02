package me.proton.pass.presentation.home

private const val ALL_ITEMS = "all_items"
private const val LOGINS = "logins"
private const val ALIASES = "aliases"
private const val NOTES = "notes"

enum class HomeFilterMode {
    AllItems,
    Logins,
    Aliases,
    Notes;

    fun value(): String = when (this) {
        AllItems -> ALL_ITEMS
        Logins -> LOGINS
        Aliases -> ALIASES
        Notes -> NOTES
    }

    companion object {
        fun fromValue(value: String): HomeFilterMode = when (value) {
            ALL_ITEMS -> AllItems
            LOGINS -> Logins
            ALIASES -> Aliases
            NOTES -> Notes
            else -> throw IllegalStateException("Unknown HomeFilterMode: $value")
        }
    }
}
