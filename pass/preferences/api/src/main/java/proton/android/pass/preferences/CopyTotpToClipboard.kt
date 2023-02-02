package proton.android.pass.preferences

sealed interface CopyTotpToClipboard {
    object Enabled : CopyTotpToClipboard
    object Not : CopyTotpToClipboard

    companion object {
        fun from(value: Boolean): CopyTotpToClipboard = if (value) {
            Enabled
        } else {
            Not
        }
    }
}

fun CopyTotpToClipboard.value(): Boolean =
    when (this) {
        CopyTotpToClipboard.Enabled -> true
        CopyTotpToClipboard.Not -> false
    }
