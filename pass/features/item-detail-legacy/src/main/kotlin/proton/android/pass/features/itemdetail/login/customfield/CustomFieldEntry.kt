/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.itemdetail.login.customfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.features.itemdetail.login.CustomFieldUiContent

@Composable
fun CustomFieldEntry(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent,
    onToggleVisibility: () -> Unit,
    onCopyValue: () -> Unit,
    onCopyValueWithContent: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    when (entry) {
        is CustomFieldUiContent.Text -> CustomFieldText(
            modifier = modifier,
            entry = entry,
            onCopyValue = onCopyValueWithContent
        )
        is CustomFieldUiContent.Hidden -> CustomFieldHidden(
            modifier = modifier,
            entry = entry,
            onToggleVisibility = onToggleVisibility,
            onCopyValue = onCopyValue
        )
        is CustomFieldUiContent.Totp -> CustomFieldTotp(
            modifier = modifier,
            entry = entry,
            onCopyTotpClick = onCopyValueWithContent
        )

        is CustomFieldUiContent.Limited -> CustomFieldLimited(
            modifier = modifier,
            entry = entry,
            onUpgrade = onUpgradeClick
        )
    }
}
