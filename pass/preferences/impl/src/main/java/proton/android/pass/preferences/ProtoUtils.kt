package proton.android.pass.preferences

import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.android.pass.preferences.LockAppPrefProto
import me.proton.android.pass.preferences.PasswordGenerationPrefProto
import me.proton.android.pass.preferences.PasswordGenerationMode as ProtoPasswordGenerationMode
import me.proton.android.pass.preferences.WordSeparator as ProtoWordSeparator

private object PasswordDefaults {
    val PASSWORD_DEFAULT_MODE = PasswordGenerationMode.Words

    object Words {
        const val COUNT = 4
        const val CAPITALIZE = true
        const val INCLUDE_NUMBER = true
        val WORD_SEPARATOR = WordSeparator.Hyphen
    }

    object Random {
        const val LENGTH = 12
        const val SPECIAL_CHARACTERS = true
        const val INCLUDE_NUMBER = true
        const val CAPITAL_LETTERS = true
    }
}


fun Boolean.toBooleanPrefProto() = if (this) {
    BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE
} else {
    BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE
}

fun fromBooleanPrefProto(pref: BooleanPrefProto, default: Boolean = false) =
    when (pref) {
        BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> true
        BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> false
        else -> default
    }

fun AppLockPreference.toProto() = when (this) {
    AppLockPreference.Immediately -> LockAppPrefProto.LOCK_APP_IMMEDIATELY
    AppLockPreference.Never -> LockAppPrefProto.LOCK_APP_NEVER
    AppLockPreference.InOneMinute -> LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE
    AppLockPreference.InTwoMinutes -> LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES
    AppLockPreference.InFiveMinutes -> LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES
    AppLockPreference.InTenMinutes -> LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES
    AppLockPreference.InOneHour -> LockAppPrefProto.LOCK_APP_IN_ONE_HOUR
    AppLockPreference.InFourHours -> LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS
}

fun LockAppPrefProto.toValue(default: AppLockPreference) = when (this) {
    LockAppPrefProto.LOCK_APP_IMMEDIATELY -> AppLockPreference.Immediately
    LockAppPrefProto.LOCK_APP_NEVER -> AppLockPreference.Never
    LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE -> AppLockPreference.InOneMinute
    LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES -> AppLockPreference.InTwoMinutes
    LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES -> AppLockPreference.InFiveMinutes
    LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES -> AppLockPreference.InTenMinutes
    LockAppPrefProto.LOCK_APP_IN_ONE_HOUR -> AppLockPreference.InOneHour
    LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS -> AppLockPreference.InFourHours
    else -> default
}

fun PasswordGenerationPreference.toProto(): PasswordGenerationPrefProto = PasswordGenerationPrefProto.newBuilder()
    .setMode(
        when (mode) {
            PasswordGenerationMode.Random -> ProtoPasswordGenerationMode.PASSWORD_GENERATION_MODE_RANDOM
            PasswordGenerationMode.Words -> ProtoPasswordGenerationMode.PASSWORD_GENERATION_MODE_WORDS
        }
    )
    .setRandomPasswordLength(randomPasswordLength)
    .setRandomHasSpecialCharacters(randomHasSpecialCharacters.toBooleanPrefProto())
    .setRandomIncludeCapitalLetters(randomHasCapitalLetters.toBooleanPrefProto())
    .setRandomIncludeNumbers(randomIncludeNumbers.toBooleanPrefProto())
    .setWordsCount(wordsCount)
    .setWordsSeparator(
        when (wordsSeparator) {
            WordSeparator.Hyphen -> ProtoWordSeparator.WORD_SEPARATOR_HYPHEN
            WordSeparator.Space -> ProtoWordSeparator.WORD_SEPARATOR_SPACE
            WordSeparator.Period -> ProtoWordSeparator.WORD_SEPARATOR_PERIOD
            WordSeparator.Comma -> ProtoWordSeparator.WORD_SEPARATOR_COMMA
            WordSeparator.Underscore -> ProtoWordSeparator.WORD_SEPARATOR_UNDERSCORE
            WordSeparator.Numbers -> ProtoWordSeparator.WORD_SEPARATOR_NUMBERS
            WordSeparator.NumbersAndSymbols -> ProtoWordSeparator.WORD_SEPARATOR_NUMBERS_AND_SYMBOLS
        }
    )
    .setWordsCapitalise(wordsCapitalise.toBooleanPrefProto())
    .setWordsIncludeNumbers(wordsIncludeNumbers.toBooleanPrefProto())
    .build()

@Suppress("MagicNumber")
fun PasswordGenerationPrefProto.toValue() = PasswordGenerationPreference(
    mode = when (mode) {
        ProtoPasswordGenerationMode.PASSWORD_GENERATION_MODE_RANDOM -> PasswordGenerationMode.Random
        ProtoPasswordGenerationMode.PASSWORD_GENERATION_MODE_WORDS -> PasswordGenerationMode.Words
        else -> PasswordDefaults.PASSWORD_DEFAULT_MODE
    },
    randomPasswordLength = if (randomPasswordLength > 3) {
        randomPasswordLength
    } else {
        PasswordDefaults.Random.LENGTH
    },
    randomHasSpecialCharacters = fromBooleanPrefProto(
        pref = randomHasSpecialCharacters,
        default = PasswordDefaults.Random.SPECIAL_CHARACTERS
    ),
    randomHasCapitalLetters = fromBooleanPrefProto(
        pref = randomIncludeCapitalLetters,
        default = PasswordDefaults.Random.CAPITAL_LETTERS
    ),
    randomIncludeNumbers = fromBooleanPrefProto(
        pref = randomIncludeNumbers,
        default = PasswordDefaults.Random.INCLUDE_NUMBER
    ),
    wordsCount = if (wordsCount > 0) {
        wordsCount
    } else PasswordDefaults.Words.COUNT,
    wordsSeparator = when (wordsSeparator) {
        ProtoWordSeparator.WORD_SEPARATOR_HYPHEN -> WordSeparator.Hyphen
        ProtoWordSeparator.WORD_SEPARATOR_SPACE -> WordSeparator.Space
        ProtoWordSeparator.WORD_SEPARATOR_PERIOD -> WordSeparator.Period
        ProtoWordSeparator.WORD_SEPARATOR_COMMA -> WordSeparator.Comma
        ProtoWordSeparator.WORD_SEPARATOR_UNDERSCORE -> WordSeparator.Underscore
        ProtoWordSeparator.WORD_SEPARATOR_NUMBERS -> WordSeparator.Numbers
        ProtoWordSeparator.WORD_SEPARATOR_NUMBERS_AND_SYMBOLS -> WordSeparator.NumbersAndSymbols
        else -> PasswordDefaults.Words.WORD_SEPARATOR
    },
    wordsCapitalise = fromBooleanPrefProto(
        pref = wordsCapitalise,
        default = PasswordDefaults.Words.CAPITALIZE
    ),
    wordsIncludeNumbers = fromBooleanPrefProto(
        pref = wordsIncludeNumbers,
        default = PasswordDefaults.Words.INCLUDE_NUMBER
    ),
)

