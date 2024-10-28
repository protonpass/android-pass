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

package proton.android.pass.features.password.dialog.separator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.password.R

@Composable
internal fun WordSeparatorDialogContent(
    modifier: Modifier = Modifier,
    state: WordSeparatorUiState,
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
            onSelected = onOptionSelected
        )
    }
}
