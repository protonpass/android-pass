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

package proton.android.pass.features.itemdetail.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent
import proton.android.pass.composecomponents.impl.attachments.AttachmentSection
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsHistorySection
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailsMoreInfoSection
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Share
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemdetail.common.ItemTitleInput
import proton.android.pass.features.itemdetail.common.ItemTitleText
import proton.android.pass.features.itemdetail.common.ThemeItemTitleProvider
import proton.android.pass.features.itemdetail.common.VaultNameSubtitle

@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    share: Share,
    isPinned: Boolean,
    canViewItemHistory: Boolean,
    isFileAttachmentsEnabled: Boolean,
    attachmentsState: AttachmentsState,
    hasMoreThanOneVaultShare: Boolean,
    onShareClick: () -> Unit,
    onViewItemHistoryClicked: () -> Unit,
    onAttachmentEvent: (AttachmentContentEvent) -> Unit
) {
    val contents = itemUiModel.contents as ItemContents.Note

    Column(
        modifier = modifier.padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                AnimatedVisibility(
                    visible = isPinned,
                    enter = expandHorizontally()
                ) {
                    CircledBadge(
                        ratio = 1f,
                        backgroundColor = PassTheme.colors.noteInteractionNormMajor1
                    )
                }

                ItemTitleText(text = contents.title, maxLines = Int.MAX_VALUE)
            }

            VaultNameSubtitle(
                share = share,
                onClick = onShareClick,
                hasMoreThanOneVaultShare = hasMoreThanOneVaultShare
            )
        }

        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = contents.note,
                style = ProtonTheme.typography.defaultNorm
            )
        }

        if (isFileAttachmentsEnabled) {
            AttachmentSection(
                attachmentsState = attachmentsState,
                isDetail = true,
                itemColors = passItemColors(ItemCategory.Note),
                onEvent = { onAttachmentEvent(it) }
            )
        }

        PassItemDetailsHistorySection(
            lastAutofillAtOption = itemUiModel.lastAutofillTime.toOption(),
            revision = itemUiModel.revision,
            createdAt = itemUiModel.createTime,
            modifiedAt = itemUiModel.modificationTime,
            onViewItemHistoryClicked = onViewItemHistoryClicked,
            itemColors = passItemColors(itemCategory = ItemCategory.Note),
            shouldDisplayItemHistoryButton = canViewItemHistory
        )

        PassItemDetailsMoreInfoSection(
            itemId = itemUiModel.id,
            shareId = itemUiModel.shareId
        )
    }
}

@Preview
@Composable
fun NoteContentPreview(@PreviewParameter(ThemeItemTitleProvider::class) input: Pair<Boolean, ItemTitleInput>) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            NoteContent(
                itemUiModel = params.itemUiModel,
                share = params.share,
                onShareClick = {},
                isPinned = params.isPinned,
                onViewItemHistoryClicked = {},
                canViewItemHistory = params.isHistoryFeatureEnabled,
                isFileAttachmentsEnabled = false,
                attachmentsState = AttachmentsState.Initial,
                hasMoreThanOneVaultShare = true,
                onAttachmentEvent = { }
            )
        }
    }
}
