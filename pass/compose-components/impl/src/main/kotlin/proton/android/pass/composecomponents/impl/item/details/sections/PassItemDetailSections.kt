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
import kotlinx.collections.immutable.toImmutableMap
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.sections.alias.PassAliasItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.cards.PassCreditCardItemDetailsSections
import proton.android.pass.composecomponents.impl.item.details.sections.custom.PassCustomItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.custom.PassSSHKeyItemDetailSections
import proton.android.pass.composecomponents.impl.item.details.sections.custom.PassWifiNetworkItemDetailSections
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
    shouldDisplayItemHistorySection: Boolean,
    shouldDisplayItemHistoryButton: Boolean,
    shouldDisplayFileAttachments: Boolean
) = with(itemDetailState) {
    when (this) {
        is ItemDetailState.Alias -> PassAliasItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            mailboxes = mailboxes.toImmutableList(),
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.CreditCard -> PassCreditCardItemDetailsSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.Identity -> PassIdentityItemDetailsSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.Login -> PassLoginItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            passwordStrength = passwordStrength,
            primaryTotp = primaryTotp,
            secondaryTotps = secondaryTotps.toImmutableMap(),
            passkeys = passkeys.toImmutableList(),
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.SSHKey -> PassSSHKeyItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.WifiNetwork -> PassWifiNetworkItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.Custom -> PassCustomItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.Note -> PassNoteItemDetailSections(
            modifier = modifier,
            itemId = itemId,
            shareId = shareId,
            contents = itemContents,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            onEvent = onEvent,
            lastAutofillOption = itemLastAutofillAtOption,
            revision = itemRevision,
            createdAt = itemCreatedAt,
            modifiedAt = itemModifiedAt,
            attachmentsState = attachmentsState,
            shouldDisplayItemHistorySection = shouldDisplayItemHistorySection,
            shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton,
            shouldDisplayFileAttachments = shouldDisplayFileAttachments
        )

        is ItemDetailState.Unknown -> itemContents.note.let { note ->
            if (note.isNotBlank()) {
                PassSharedItemDetailNoteSection(
                    note = note,
                    itemColors = itemColors,
                    itemDiffs = itemDiffs
                )
            }
        }
    }
}
