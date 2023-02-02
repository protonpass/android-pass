package proton.android.pass.preferences

sealed interface CopyTotpToClipboard {
    object Enabled : CopyTotpToClipboard
    object NotEnabled : CopyTotpToClipboard

    companion object {
        fun from(value: Boolean): CopyTotpToClipboard = if (value) {
            Enabled
        } else {
            NotEnabled
        }
    }
}

fun CopyTotpToClipboard.value(): Boolean =
    when (this) {
        CopyTotpToClipboard.Enabled -> true
        CopyTotpToClipboard.NotEnabled -> false
    }
