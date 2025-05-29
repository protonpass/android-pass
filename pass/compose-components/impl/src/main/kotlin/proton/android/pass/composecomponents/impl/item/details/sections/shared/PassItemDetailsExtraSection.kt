/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.TotpState

@Composable
internal fun PassItemDetailsExtraSection(
    modifier: Modifier = Modifier,
    extraSectionContents: ImmutableList<ExtraSectionContent>,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    extraSectionContents.forEachIndexed { extraSectionIndex, extraSectionContent ->
        if (extraSectionContent.hasCustomFields) {
            val rows = mutableListOf<@Composable () -> Unit>()
            val customFieldSection = when (itemDiffs) {
                is ItemDiffs.Identity,
                is ItemDiffs.WifiNetwork,
                is ItemDiffs.SSHKey,
                is ItemDiffs.Custom -> ItemSection.ExtraSection(extraSectionIndex)
                is ItemDiffs.Login,
                ItemDiffs.None,
                is ItemDiffs.Note,
                is ItemDiffs.Alias,
                is ItemDiffs.CreditCard,
                is ItemDiffs.Unknown ->
                    throw UnsupportedOperationException("type unsupported ${itemDiffs::class.simpleName} ")
            }
            rows.addCustomFieldRows(
                customFields = extraSectionContent.customFieldList,
                customFieldSection = customFieldSection,
                customFieldTotps = customFieldTotps,
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )

            PassItemDetailsSection(
                modifier = modifier,
                title = extraSectionContent.title,
                sections = rows.toPersistentList()
            )
        }
    }
}
