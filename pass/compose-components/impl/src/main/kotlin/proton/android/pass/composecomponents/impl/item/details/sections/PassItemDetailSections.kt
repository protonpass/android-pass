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

package proton.android.pass.composecomponents.impl.item.details.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.alias.PassAliasItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.cards.PassCreditCardItemDetailsSections
import proton.android.pass.composecomponents.impl.item.details.sections.identity.PassIdentityItemDetailsSections
import proton.android.pass.composecomponents.impl.item.details.sections.login.PassLoginItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.notes.PassNoteItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors

@Composable
internal fun PassItemDetailSections(
    modifier: Modifier = Modifier,
    itemDetailState: ItemDetailState,
    itemColors: PassItemColors,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    shouldDisplayItemHistorySection: Boolean
) = with(itemDetailState) {
    when (this) {
        is ItemDetailState.Alias -> PassAliasItemDetailSections(
            modifier = modifier,
            contents = itemContents,
            itemColors = itemColors,
            mailboxes = mailboxes.toImmutableList(),
            onEvent = onEvent
        )

        is ItemDetailState.CreditCard -> PassCreditCardItemDetailsSections(
            modifier = modifier,
            contents = itemContents,
            itemColors = itemColors,
            onEvent = onEvent
        )

        is ItemDetailState.Identity -> PassIdentityItemDetailsSections(
            modifier = modifier,
            contents = itemContents,
            itemColors = itemColors,
            onEvent = onEvent,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection
        )

        is ItemDetailState.Login -> PassLoginItemDetailSections(
            modifier = modifier,
            contents = itemContents,
            passwordStrength = passwordStrength,
            primaryTotp = primaryTotp,
            customFields = customFields.toImmutableList(),
            passkeys = passkeys.toImmutableList(),
            itemColors = itemColors,
            onEvent = onEvent,
            isUsernameSplitEnabled = isUsernameSplitEnabled
        )

        is ItemDetailState.Note -> PassNoteItemDetailSections(
            modifier = modifier,
            contents = itemContents
        )

        is ItemDetailState.Unknown -> itemContents.note.let { note ->
            if (note.isNotBlank()) {
                PassSharedItemDetailNoteSection(
                    note = note,
                    itemColors = itemColors
                )
            }
        }
    }
}
