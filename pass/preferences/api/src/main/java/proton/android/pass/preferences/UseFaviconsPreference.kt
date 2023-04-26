package proton.android.pass.preferences

sealed interface UseFaviconsPreference {
    object Enabled : UseFaviconsPreference
    object Disabled : UseFaviconsPreference

    companion object {
        fun from(value: Boolean): UseFaviconsPreference = if (value) { Enabled } else { Disabled }
    }
}

fun UseFaviconsPreference.value(): Boolean =
    when (this) {
        UseFaviconsPreference.Enabled -> true
        UseFaviconsPreference.Disabled -> false
    }
