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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.itemdetail.login.CustomFieldUiContent

@Composable
fun CustomFieldDetails(
    modifier: Modifier = Modifier,
    fields: List<CustomFieldUiContent>,
    onEvent: (CustomFieldEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        fields.forEachIndexed { idx, entry ->
            CustomFieldEntry(
                entry = entry,
                onToggleVisibility = {
                    onEvent(CustomFieldEvent.ToggleFieldVisibility(idx))
                },
                onCopyValue = {
                    onEvent(CustomFieldEvent.CopyValue(idx))
                },
                onCopyValueWithContent = { onEvent(CustomFieldEvent.CopyValueContent(it)) },
                onUpgradeClick = { onEvent(CustomFieldEvent.Upgrade) }
            )
        }
    }
}
