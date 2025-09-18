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

package proton.android.pass.composecomponents.impl.item.details.sections.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType.AliasItemAction.ContactBanner
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType.AliasItemAction.ContactSection
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent.OnAttachmentEvent
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailCustomFieldsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassSharedItemDetailNoteSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasStats
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.VaultId

@Composable
internal fun PassAliasItemDetailSections(
    modifier: Modifier = Modifier,
    itemId: ItemId,
    shareId: ShareId,
    vaultId: VaultId,
    contents: ItemContents.Alias,
    isAliasCreatedByUser: Boolean,
    isAliasStateToggling: Boolean,
    slNote: String,
    displayName: String,
    stats: Option<AliasStats>,
    contactsCount: Int,
    displayContactsBanner: Boolean,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Alias,
    mailboxes: ImmutableList<AliasMailbox>,
    lastAutofillOption: Option<Instant>,
    revision: Long,
    createdAt: Instant,
    modifiedAt: Instant,
    shouldDisplayItemHistorySection: Boolean,
    shouldDisplayItemHistoryButton: Boolean,
    attachmentsState: AttachmentsState,
    customFieldTotps: ImmutableMap<Pair<Option<Int>, Int>, TotpState>,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(contents) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        PassAliasItemDetailMainSection(
            shareId = shareId,
            itemId = itemId,
            alias = aliasEmail,
            isAliasEnabled = isEnabled,
            isAliasCreatedByUser = isAliasCreatedByUser,
            isAliasStateToggling = isAliasStateToggling,
            itemColors = itemColors,
            itemDiffs = itemDiffs,
            mailboxes = mailboxes,
            onEvent = onEvent
        )

        if (note.isNotBlank()) {
            PassSharedItemDetailNoteSection(
                note = note,
                itemColors = itemColors,
                itemDiffs = itemDiffs
            )
        }

        if (slNote.isNotBlank()) {
            PassSharedItemDetailNoteSection(
                title = buildString {
                    append(stringResource(id = R.string.item_details_shared_section_note_title))
                    append(" ${SpecialCharacters.DOT_SEPARATOR} ")
                    append(stringResource(id = R.string.simple_login_brand_name))
                },
                note = slNote,
                itemColors = itemColors,
                itemDiffs = itemDiffs
            )
        }

        if (isAliasCreatedByUser) {
            PassAliasSenderNameSection(text = displayName)

            PassAliasContactsSection(
                modifier = Modifier.padding(bottom = Spacing.small),
                displayContactsBanner = displayContactsBanner,
                counter = contactsCount,
                onClick = {
                    onEvent(PassItemDetailsUiEvent.OnFieldClick(ContactSection(shareId, itemId)))
                },
                onDismiss = { onEvent(PassItemDetailsUiEvent.OnFieldClick(ContactBanner)) }
            )
        }

        if (stats is Some) {
            PassAliasStatsSection(stats = stats.value)
        }

        if (contents.customFields.isNotEmpty()) {
            PassItemDetailCustomFieldsSection(
                customFields = contents.customFields.toPersistentList(),
                customFieldTotps = customFieldTotps,
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )
        }

        AttachmentSection(
            attachmentsState = attachmentsState,
            isDetail = true,
            itemColors = itemColors,
            itemDiffs = itemDiffs.attachments,
            onEvent = { onEvent(OnAttachmentEvent(it)) }
        )

        if (shouldDisplayItemHistorySection) {
            PassItemDetailsHistorySection(
                lastAutofillAtOption = lastAutofillOption,
                revision = revision,
                createdAt = createdAt,
                modifiedAt = modifiedAt,
                itemColors = itemColors,
                onViewItemHistoryClicked = { onEvent(PassItemDetailsUiEvent.OnViewItemHistoryClick) },
                shouldDisplayItemHistoryButton = shouldDisplayItemHistoryButton
            )
        }

        PassItemDetailsMoreInfoSection(
            itemId = itemId,
            shareId = shareId,
            vaultId = vaultId
        )
    }
}
