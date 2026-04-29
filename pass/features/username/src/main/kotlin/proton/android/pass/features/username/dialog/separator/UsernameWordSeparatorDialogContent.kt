/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username.dialog.separator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.dialogs.WordSeparatorList

@Composable
internal fun UsernameWordSeparatorDialogContent(
    modifier: Modifier = Modifier,
    state: UsernameWordSeparatorUiState,
    onOptionSelected: (PasswordWordSeparator) -> Unit
) = with(state) {
    Column(
        modifier = modifier.padding(vertical = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        ProtonDialogTitle(
            modifier = Modifier.padding(start = Spacing.large),
            title = stringResource(R.string.word_separator)
        )

        WordSeparatorList(
            options = options,
            selected = selected,
            label = { it.toSharedLabel() },
            onSelected = onOptionSelected
        )
    }
}

@Composable
private fun PasswordWordSeparator.toSharedLabel(): String = stringResource(
    when (this) {
        PasswordWordSeparator.Hyphen -> R.string.bottomsheet_option_word_separator_hyphens
        PasswordWordSeparator.Space -> R.string.bottomsheet_option_word_separator_spaces
        PasswordWordSeparator.Period -> R.string.bottomsheet_option_word_separator_periods
        PasswordWordSeparator.Comma -> R.string.bottomsheet_option_word_separator_commas
        PasswordWordSeparator.Underscore -> R.string.bottomsheet_option_word_separator_underscores
        PasswordWordSeparator.Numbers -> R.string.bottomsheet_option_word_separator_numbers
        PasswordWordSeparator.NumbersAndSymbols ->
            R.string.bottomsheet_option_word_separator_numbers_and_symbols
    }
)
