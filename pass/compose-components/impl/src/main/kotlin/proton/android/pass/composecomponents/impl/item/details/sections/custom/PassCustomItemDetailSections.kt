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

package proton.android.pass.composecomponents.impl.item.details.sections.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent.OnAttachmentEvent
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailCustomFieldsSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsExtraSection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

@Composable
internal fun PassCustomItemDetailSections(
    modifier: Modifier = Modifier,
    itemId: ItemId,
    shareId: ShareId,
    contents: ItemContents,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs,
    lastAutofillOption: Option<Instant>,
    revision: Long,
    createdAt: Instant,
    modifiedAt: Instant,
    shouldDisplayItemHistorySection: Boolean,
    shouldDisplayItemHistoryButton: Boolean,
    shouldDisplayFileAttachments: Boolean,
    attachmentsState: AttachmentsState,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    val (customFieldList, sectionContentList) = when (contents) {
        is ItemContents.Custom -> contents.customFieldList to contents.sectionContentList
        is ItemContents.SSHKey -> contents.customFieldList to contents.sectionContentList
        is ItemContents.WifiNetwork -> contents.customFieldList to contents.sectionContentList
        else -> throw IllegalStateException("Item type not supported: $contents")
    }
    return Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        if (customFieldList.isNotEmpty()) {
            PassItemDetailCustomFieldsSection(
                customFields = customFieldList.toPersistentList(),
                secondaryTotps = persistentMapOf(),
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )
        }

        if (sectionContentList.isNotEmpty()) {
            PassItemDetailsExtraSection(
                extraSectionContents = sectionContentList.toPersistentList(),
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = onEvent
            )
        }

        if (shouldDisplayFileAttachments) {
            AttachmentSection(
                attachmentsState = attachmentsState,
                isDetail = true,
                itemColors = itemColors,
                itemDiffs = itemDiffs,
                onEvent = { onEvent(OnAttachmentEvent(it)) }
            )
        }

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
            shareId = shareId
        )
    }
}
