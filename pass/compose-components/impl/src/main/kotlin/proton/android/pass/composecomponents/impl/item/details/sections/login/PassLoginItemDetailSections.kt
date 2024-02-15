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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.ItemContents

@Composable
internal fun PassLoginItemDetailSections(
    modifier: Modifier = Modifier,
    contents: ItemContents.Login,
    itemColors: ProtonItemColors,
) = with(contents) {
    PassLoginItemDetailMainSection(
        modifier = modifier,
        username = username,
        itemColors = itemColors,
    )

    if (urls.isNotEmpty()) {
        PassLoginItemDetailWebsitesSection(
            modifier = modifier,
            urls = urls.toPersistentList(),
            itemColors = itemColors,
        )
    }

    if (note.isNotBlank()) {
        PassSharedItemDetailNoteSection(
            modifier = modifier,
            note = note,
            itemColors = itemColors,
        )
    }
}
