/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.features.password.R
import proton.android.pass.preferences.PasswordGenerationMode
import proton.android.pass.preferences.WordSeparator
import proton.android.pass.commonrust.api.WordSeparator as ApiWordSeparator

fun WordSeparator.toDomain(): ApiWordSeparator = when (this) {
    WordSeparator.Hyphen -> ApiWordSeparator.Hyphen
    WordSeparator.Space -> ApiWordSeparator.Space
    WordSeparator.Period -> ApiWordSeparator.Period
    WordSeparator.Comma -> ApiWordSeparator.Comma
    WordSeparator.Underscore -> ApiWordSeparator.Underscore
    WordSeparator.Numbers -> ApiWordSeparator.Numbers
    WordSeparator.NumbersAndSymbols -> ApiWordSeparator.NumbersAndSymbols
}

fun ApiWordSeparator.toPassword(): WordSeparator = when (this) {
    ApiWordSeparator.Hyphen -> WordSeparator.Hyphen
    ApiWordSeparator.Space -> WordSeparator.Space
    ApiWordSeparator.Period -> WordSeparator.Period
    ApiWordSeparator.Comma -> WordSeparator.Comma
    ApiWordSeparator.Underscore -> WordSeparator.Underscore
    ApiWordSeparator.Numbers -> WordSeparator.Numbers
    ApiWordSeparator.NumbersAndSymbols -> WordSeparator.NumbersAndSymbols
}

@Composable
fun ApiWordSeparator.toResourceString() = when (this) {
    ApiWordSeparator.Hyphen -> stringResource(R.string.bottomsheet_option_word_separator_hyphens)
    ApiWordSeparator.Space -> stringResource(R.string.bottomsheet_option_word_separator_spaces)
    ApiWordSeparator.Period -> stringResource(R.string.bottomsheet_option_word_separator_periods)
    ApiWordSeparator.Comma -> stringResource(R.string.bottomsheet_option_word_separator_commas)
    ApiWordSeparator.Underscore -> stringResource(R.string.bottomsheet_option_word_separator_underscores)
    ApiWordSeparator.Numbers -> stringResource(R.string.bottomsheet_option_word_separator_numbers)
    ApiWordSeparator.NumbersAndSymbols ->
        stringResource(R.string.bottomsheet_option_word_separator_numbers_and_symbols)
}

@Composable
fun PasswordGenerationMode.toResourceString() = when (this) {
    PasswordGenerationMode.Words -> stringResource(R.string.password_mode_memorable)
    PasswordGenerationMode.Random -> stringResource(R.string.password_mode_random)
}
