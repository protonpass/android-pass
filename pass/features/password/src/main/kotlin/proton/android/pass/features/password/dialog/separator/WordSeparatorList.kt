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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.features.password.dialog.DialogOptionRow
import proton.android.pass.features.password.extensions.toResourceString

@Composable
fun WordSeparatorList(
    modifier: Modifier = Modifier,
    options: PersistentList<PasswordWordSeparator>,
    selected: Option<PasswordWordSeparator>,
    onSelected: (PasswordWordSeparator) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items = options, key = { it.name }) { option ->
            DialogOptionRow(
                value = option.toResourceString(),
                isSelected = selected.value() == option,
                onClick = { onSelected(option) }
            )
        }
    }
}
