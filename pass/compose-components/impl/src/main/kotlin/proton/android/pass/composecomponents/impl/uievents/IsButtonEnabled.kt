package proton.android.pass.composecomponents.impl.uievents

sealed interface IsButtonEnabled {
    object Enabled : IsButtonEnabled
    object Disabled : IsButtonEnabled

    companion object {
        fun from(value: Boolean): IsButtonEnabled = if (value) { Enabled } else { Disabled }
    }
}

fun IsButtonEnabled.value(): Boolean =
    when (this) {
        IsButtonEnabled.Enabled -> true
        IsButtonEnabled.Disabled -> false
    }

