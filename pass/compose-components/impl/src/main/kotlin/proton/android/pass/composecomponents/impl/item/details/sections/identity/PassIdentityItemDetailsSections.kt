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

package proton.android.pass.composecomponents.impl.item.details.sections.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Instant
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemContents

@Composable
internal fun PassIdentityItemDetailsSections(
    modifier: Modifier = Modifier,
    contents: ItemContents.Identity,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    createdAt: Instant,
    modifiedAt: Instant,
    shouldDisplayItemHistorySection: Boolean
) = with(contents) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        if (personalDetailsContent.hasPersonalDetails) {
            PassIdentityItemDetailsPersonalSection(
                personalDetailsContent = personalDetailsContent,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (addressDetailsContent.hasAddressDetails) {
            PassIdentityItemDetailsAddressSection(
                addressDetailsContent = addressDetailsContent,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (contactDetailsContent.hasContactDetails) {
            PassIdentityItemDetailsContactSection(
                contactDetailsContent = contactDetailsContent,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (workDetailsContent.hasWorkDetails) {
            PassIdentityItemDetailsWorkSection(
                workDetailsContent = workDetailsContent,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (extraSectionContentList.isNotEmpty()) {
            PassIdentityItemDetailsExtraSection(
                extraSectionContents = extraSectionContentList.toPersistentList(),
                itemColors = itemColors,
                onEvent = onEvent
            )
        }

        if (shouldDisplayItemHistorySection) {
            PassItemDetailsHistorySection(
                modifier = Modifier.padding(vertical = Spacing.extraSmall),
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                itemColors = itemColors,
                onEvent = onEvent
            )
        }
    }
}
