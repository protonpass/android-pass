/*
 * Copyright (c) 2023-2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.preferences

import me.proton.android.pass.preferences.AppLockTypePrefProto
import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.android.pass.preferences.LockAppPrefProto
import me.proton.android.pass.preferences.MonitorStatusPrefProto
import me.proton.android.pass.preferences.PasswordGenerationPrefProto
import me.proton.android.pass.preferences.UsernameGenerationPrefProto
import me.proton.android.pass.preferences.UsernameWordTypesPrefProto
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import me.proton.android.pass.preferences.PasswordGenerationMode as ProtoPasswordGenerationMode
import me.proton.android.pass.preferences.WordSeparator as ProtoWordSeparator

private object PasswordDefaults {
    val PASSWORD_DEFAULT_MODE = PasswordGenerationMode.Words

    object Words {
        const val COUNT = 5
        const val CAPITALIZE = true
        const val INCLUDE_NUMBER = true
        val WORD_SEPARATOR = WordSeparator.Hyphen
    }

    object Random {
        const val LENGTH = 20
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

fun fromBooleanPrefProto(pref: BooleanPrefProto, default: Boolean = false) = when (pref) {
    BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> true
    BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> false
    else -> default
}

fun AppLockTimePreference.toProto() = when (this) {
    AppLockTimePreference.Immediately -> LockAppPrefProto.LOCK_APP_IMMEDIATELY
    AppLockTimePreference.InOneMinute -> LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE
    AppLockTimePreference.InTwoMinutes -> LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES
    AppLockTimePreference.InFiveMinutes -> LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES
    AppLockTimePreference.InTenMinutes -> LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES
    AppLockTimePreference.InOneHour -> LockAppPrefProto.LOCK_APP_IN_ONE_HOUR
    AppLockTimePreference.InFourHours -> LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS
}

fun LockAppPrefProto.toValue(default: AppLockTimePreference) = when (this) {
    LockAppPrefProto.LOCK_APP_IMMEDIATELY -> AppLockTimePreference.Immediately
    LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE -> AppLockTimePreference.InOneMinute
    LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES -> AppLockTimePreference.InTwoMinutes
    LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES -> AppLockTimePreference.InFiveMinutes
    LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES -> AppLockTimePreference.InTenMinutes
    LockAppPrefProto.LOCK_APP_IN_ONE_HOUR -> AppLockTimePreference.InOneHour
    LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS -> AppLockTimePreference.InFourHours
    else -> default
}

fun AppLockTypePreference.toProto() = when (this) {
    AppLockTypePreference.Biometrics -> AppLockTypePrefProto.APP_LOCK_TYPE_BIOMETRICS
    AppLockTypePreference.Pin -> AppLockTypePrefProto.APP_LOCK_TYPE_PIN
    AppLockTypePreference.None -> AppLockTypePrefProto.APP_LOCK_TYPE_UNSPECIFIED
}

fun AppLockTypePrefProto.toValue(default: AppLockTypePreference) = when (this) {
    AppLockTypePrefProto.APP_LOCK_TYPE_BIOMETRICS -> AppLockTypePreference.Biometrics
    AppLockTypePrefProto.APP_LOCK_TYPE_PIN -> AppLockTypePreference.Pin
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
    )
)

private fun WordSeparator.toProtoWordSeparator(): ProtoWordSeparator = when (this) {
    WordSeparator.Hyphen -> ProtoWordSeparator.WORD_SEPARATOR_HYPHEN
    WordSeparator.Space -> ProtoWordSeparator.WORD_SEPARATOR_SPACE
    WordSeparator.Period -> ProtoWordSeparator.WORD_SEPARATOR_PERIOD
    WordSeparator.Comma -> ProtoWordSeparator.WORD_SEPARATOR_COMMA
    WordSeparator.Underscore -> ProtoWordSeparator.WORD_SEPARATOR_UNDERSCORE
    WordSeparator.Numbers -> ProtoWordSeparator.WORD_SEPARATOR_NUMBERS
    WordSeparator.NumbersAndSymbols -> ProtoWordSeparator.WORD_SEPARATOR_NUMBERS_AND_SYMBOLS
}

private fun ProtoWordSeparator.toDomain(default: WordSeparator): WordSeparator = when (this) {
    ProtoWordSeparator.WORD_SEPARATOR_HYPHEN -> WordSeparator.Hyphen
    ProtoWordSeparator.WORD_SEPARATOR_SPACE -> WordSeparator.Space
    ProtoWordSeparator.WORD_SEPARATOR_PERIOD -> WordSeparator.Period
    ProtoWordSeparator.WORD_SEPARATOR_COMMA -> WordSeparator.Comma
    ProtoWordSeparator.WORD_SEPARATOR_UNDERSCORE -> WordSeparator.Underscore
    ProtoWordSeparator.WORD_SEPARATOR_NUMBERS -> WordSeparator.Numbers
    ProtoWordSeparator.WORD_SEPARATOR_NUMBERS_AND_SYMBOLS -> WordSeparator.NumbersAndSymbols
    else -> default
}

fun UsernameGenerationPreference.toProto(): UsernameGenerationPrefProto = UsernameGenerationPrefProto.newBuilder()
    .setWordCount(wordCount)
    .setWordsSeparator(wordsSeparator.toProtoWordSeparator())
    .setCapitalise(capitalise.toBooleanPrefProto())
    .setIncludeNumbers(includeNumbers.toBooleanPrefProto())
    .setLeetspeak(leetspeak.toBooleanPrefProto())
    .setWordTypes(wordTypes.toProto())
    .build()

fun UsernameWordTypesPreference.toProto(): UsernameWordTypesPrefProto = UsernameWordTypesPrefProto.newBuilder()
    .setAdjectives(adjectives.toBooleanPrefProto())
    .setNouns(nouns.toBooleanPrefProto())
    .setVerbs(verbs.toBooleanPrefProto())
    .build()

fun UsernameGenerationPrefProto.toValue(): UsernameGenerationPreference {
    val default = UsernameGenerationPreference.Default
    return UsernameGenerationPreference(
        wordCount = if (wordCount > 0) wordCount else default.wordCount,
        wordsSeparator = wordsSeparator.toDomain(default.wordsSeparator),
        capitalise = fromBooleanPrefProto(capitalise, default.capitalise),
        includeNumbers = fromBooleanPrefProto(includeNumbers, default.includeNumbers),
        leetspeak = fromBooleanPrefProto(leetspeak, default.leetspeak),
        wordTypes = wordTypes.toValue(default.wordTypes)
    )
}

fun UsernameWordTypesPrefProto.toValue(default: UsernameWordTypesPreference): UsernameWordTypesPreference =
    UsernameWordTypesPreference(
        adjectives = fromBooleanPrefProto(adjectives, default.adjectives),
        nouns = fromBooleanPrefProto(nouns, default.nouns),
        verbs = fromBooleanPrefProto(verbs, default.verbs)
    )

internal fun MonitorStatusPreference.toProto(): MonitorStatusPrefProto = when (this) {
    MonitorStatusPreference.BreachIssues -> MonitorStatusPrefProto.BREACH_ISSUES
    MonitorStatusPreference.NoIssues -> MonitorStatusPrefProto.NO_ISSUES
    MonitorStatusPreference.VulnerabilityIssues -> MonitorStatusPrefProto.VULNERABILITY_ISSUES
}

internal fun MonitorStatusPrefProto.toValue(): MonitorStatusPreference = when (this) {
    MonitorStatusPrefProto.BREACH_ISSUES -> MonitorStatusPreference.BreachIssues
    MonitorStatusPrefProto.NO_ISSUES -> MonitorStatusPreference.NoIssues
    MonitorStatusPrefProto.VULNERABILITY_ISSUES -> MonitorStatusPreference.VulnerabilityIssues
    MonitorStatusPrefProto.UNRECOGNIZED -> MonitorStatusPreference.NoIssues
}
