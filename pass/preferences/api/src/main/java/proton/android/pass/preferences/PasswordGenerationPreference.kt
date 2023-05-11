package proton.android.pass.preferences

enum class WordSeparator {
    Hyphen,
    Space,
    Period,
    Comma,
    Underscore,
    Numbers,
    NumbersAndSymbols
}

enum class PasswordGenerationMode {
    Random,
    Words
}

data class PasswordGenerationPreference(
    val mode: PasswordGenerationMode,
    val randomPasswordLength: Int,
    val randomHasSpecialCharacters: Boolean,
    val wordsCount: Int,
    val wordsSeparator: WordSeparator,
    val wordsCapitalise: Boolean,
    val wordsIncludeNumbers: Boolean
)
