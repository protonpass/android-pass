package proton.android.pass.preferences

sealed interface AllowScreenshotsPreference {
    object Enabled : AllowScreenshotsPreference
    object Disabled : AllowScreenshotsPreference

    companion object {
        fun from(value: Boolean): AllowScreenshotsPreference = if (value) { Enabled } else { Disabled }
    }
}

fun AllowScreenshotsPreference.value(): Boolean =
    when (this) {
        AllowScreenshotsPreference.Enabled -> true
        AllowScreenshotsPreference.Disabled -> false
    }

