package proton.android.pass.featurepassword.impl.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.featurepassword.R
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordContent
import proton.android.pass.password.api.PasswordGenerator
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.PasswordGenerationPreference
import proton.android.pass.preferences.WordSeparator

fun PasswordGenerationPreference.toWordSpec(): PasswordGenerator.WordPasswordSpec {
    return PasswordGenerator.WordPasswordSpec(
        count = wordsCount,
        separator = wordsSeparator.toDomain(),
        capitalise = wordsCapitalise,
        includeNumbers = wordsIncludeNumbers
    )
}

fun PasswordGenerationPreference.toRandomSpec(): PasswordGenerator.RandomPasswordSpec {
    return PasswordGenerator.RandomPasswordSpec(
        length = randomPasswordLength,
        hasCapitalLetters = randomHasCapitalLetters,
        hasNumbers = randomIncludeNumbers,
        hasSymbols = randomHasSpecialCharacters
    )
}

fun WordSeparator.toDomain(): PasswordGenerator.WordSeparator = when (this) {
    WordSeparator.Hyphen -> PasswordGenerator.WordSeparator.Hyphen
    WordSeparator.Space -> PasswordGenerator.WordSeparator.Space
    WordSeparator.Period -> PasswordGenerator.WordSeparator.Period
    WordSeparator.Comma -> PasswordGenerator.WordSeparator.Comma
    WordSeparator.Underscore -> PasswordGenerator.WordSeparator.Underscore
    WordSeparator.Numbers -> PasswordGenerator.WordSeparator.Numbers
    WordSeparator.NumbersAndSymbols -> PasswordGenerator.WordSeparator.NumbersAndSymbols
}

fun PasswordGenerator.WordSeparator.toPassword(): WordSeparator = when (this) {
    PasswordGenerator.WordSeparator.Hyphen -> WordSeparator.Hyphen
    PasswordGenerator.WordSeparator.Space -> WordSeparator.Space
    PasswordGenerator.WordSeparator.Period -> WordSeparator.Period
    PasswordGenerator.WordSeparator.Comma -> WordSeparator.Comma
    PasswordGenerator.WordSeparator.Underscore -> WordSeparator.Underscore
    PasswordGenerator.WordSeparator.Numbers -> WordSeparator.Numbers
    PasswordGenerator.WordSeparator.NumbersAndSymbols -> WordSeparator.NumbersAndSymbols
}

fun PasswordGenerationPreference.toContent(): GeneratePasswordContent = when (mode) {
    PasswordGenerationMode.Words -> GeneratePasswordContent.WordsPassword(
        count = wordsCount,
        wordSeparator = wordsSeparator.toDomain(),
        capitalise = wordsCapitalise,
        includeNumbers = wordsIncludeNumbers
    )

    PasswordGenerationMode.Random -> GeneratePasswordContent.RandomPassword(
        length = randomPasswordLength,
        hasSpecialCharacters = randomHasSpecialCharacters,
        hasCapitalLetters = randomHasCapitalLetters,
        includeNumbers = randomIncludeNumbers
    )
}

@Composable
fun PasswordGenerator.WordSeparator.toResourceString() = when (this) {
    PasswordGenerator.WordSeparator.Hyphen -> stringResource(R.string.bottomsheet_option_word_separator_hyphens)
    PasswordGenerator.WordSeparator.Space -> stringResource(R.string.bottomsheet_option_word_separator_spaces)
    PasswordGenerator.WordSeparator.Period -> stringResource(R.string.bottomsheet_option_word_separator_periods)
    PasswordGenerator.WordSeparator.Comma -> stringResource(R.string.bottomsheet_option_word_separator_commas)
    PasswordGenerator.WordSeparator.Underscore -> stringResource(R.string.bottomsheet_option_word_separator_underscores)
    PasswordGenerator.WordSeparator.Numbers -> stringResource(R.string.bottomsheet_option_word_separator_numbers)
    PasswordGenerator.WordSeparator.NumbersAndSymbols ->
        stringResource(R.string.bottomsheet_option_word_separator_numbers_and_symbols)
}

@Composable
fun PasswordGenerationMode.toResourceString() = when (this) {
    PasswordGenerationMode.Words -> stringResource(R.string.password_mode_memorable)
    PasswordGenerationMode.Random -> stringResource(R.string.password_mode_random)
}
