/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents

@Composable
internal fun PassLoginItemDetailSections(
    modifier: Modifier = Modifier,
    contents: ItemContents.Login,
    passwordStrength: PasswordStrength,
    itemColors: ProtonItemColors,
    onSectionClick: (String) -> Unit,
    onHiddenSectionClick: (HiddenState) -> Unit,
    onHiddenSectionToggle: (Boolean, HiddenState, ItemDetailsFieldType.Hidden) -> Unit,
) = with(contents) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PassLoginItemDetailMainSection(
            username = username,
            password = password,
            passwordStrength = passwordStrength,
            itemColors = itemColors,
            onSectionClick = onSectionClick,
            onHiddenSectionClick = onHiddenSectionClick,
            onHiddenSectionToggle = onHiddenSectionToggle,
        )

        if (urls.isNotEmpty()) {
            PassLoginItemDetailWebsitesSection(
                urls = urls.toPersistentList(),
                itemColors = itemColors,
            )
        }

        if (note.isNotBlank()) {
            PassSharedItemDetailNoteSection(
                note = note,
                itemColors = itemColors,
            )
        }
    }
}
