package proton.android.pass.preferences

private const val CLIPBOARD_NEVER = 1
private const val CLIPBOARD_60S = 2
private const val CLIPBOARD_180S = 3

enum class ClearClipboardPreference {
    Never,
    S60,
    S180;

    fun value(): Int =
        when (this) {
            Never -> CLIPBOARD_NEVER
            S60 -> CLIPBOARD_60S
            S180 -> CLIPBOARD_180S
        }

    companion object {
        fun from(value: Int): ClearClipboardPreference =
            when (value) {
                CLIPBOARD_NEVER -> Never
                CLIPBOARD_60S -> S60
                CLIPBOARD_180S -> S180
                else -> S60
            }
    }
}
