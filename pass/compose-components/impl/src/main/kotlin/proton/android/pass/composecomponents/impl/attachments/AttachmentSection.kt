/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnAddAttachment
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnAttachmentOpen
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnAttachmentOptions
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentOpen
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentOptions
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentRetry
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.details.modifiers.contentDiff
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.items.ItemCategory

@Composable
fun AttachmentSection(
    modifier: Modifier = Modifier,
    attachmentsState: AttachmentsState,
    isDetail: Boolean,
    itemColors: PassItemColors,
    itemDiffs: Map<AttachmentId, ItemDiffType> = emptyMap(),
    isEditAllowed: Boolean = true,
    onEvent: (AttachmentContentEvent) -> Unit
) {
    if (!attachmentsState.canShowAttachmentSection(isDetail)) return
    val interactionsEnabled = attachmentsState.isEnabled && isEditAllowed
    val onGuardedEvent: (AttachmentContentEvent) -> Unit = { event ->
        if (isEditAllowed) onEvent(event)
    }
    Column(
        modifier = modifier
            .applyIf(
                condition = !isDetail,
                ifTrue = { roundedContainerNorm() },
                ifFalse = { roundedContainer(Color.Transparent, ProtonTheme.colors.separatorNorm) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        AttachmentHeader(
            modifier = Modifier.padding(
                start = Spacing.medium,
                top = Spacing.medium,
                end = Spacing.medium
            ),
            colors = itemColors,
            isEnabled = interactionsEnabled,
            fileAmount = attachmentsState.size,
            isDetail = isDetail,
            needsUpgrade = attachmentsState.needsUpgrade,
            onTrashAll = { onGuardedEvent(AttachmentContentEvent.OnDeleteAllAttachments) }
        )
        Column {
            attachmentsState.attachmentsList.forEachIndexed { index, attachment ->
                AttachmentRow(
                    innerModifier = Modifier
                        .contentDiff(itemDiffType = itemDiffs[attachment.id] ?: ItemDiffType.None)
                        .applyIf(
                            condition = isDetail,
                            ifTrue = { padding(start = Spacing.medium) },
                            ifFalse = { padding(horizontal = Spacing.medium) }
                        )
                        .padding(top = Spacing.medium)
                        .applyIf(
                            condition = attachmentsState.shouldDisplayDivider(index),
                            ifTrue = { padding(bottom = Spacing.medium) }
                        )
                        .applyIf(
                            condition = isDetail && !attachmentsState.shouldDisplayDivider(index),
                            ifTrue = { padding(bottom = Spacing.medium) }
                        ),
                    filename = attachment.name,
                    attachmentType = attachment.type,
                    size = attachment.size,
                    createTime = attachment.createTime,
                    isEnabled = interactionsEnabled,
                    isError = false,
                    isLoading = attachmentsState.loadingAttachments.contains(attachment.id),
                    onRetryClick = {},
                    onOptionsClick = {
                        onGuardedEvent(
                            OnAttachmentOptions(
                                shareId = attachment.shareId,
                                itemId = attachment.itemId,
                                attachmentId = attachment.id
                            )
                        )
                    },
                    onAttachmentOpen = { onGuardedEvent(OnAttachmentOpen(attachment)) }
                )
                if (attachmentsState.shouldDisplayDivider(index)) {
                    PassDivider()
                }
            }
            attachmentsState.draftAttachmentsList.forEachIndexed { index, draftAttachment ->
                val fileMetadata = draftAttachment.metadata
                AttachmentRow(
                    innerModifier = Modifier
                        .applyIf(
                            condition = isDetail,
                            ifTrue = { padding(start = Spacing.medium) },
                            ifFalse = { padding(horizontal = Spacing.medium) }
                        )
                        .padding(top = Spacing.medium)
                        .applyIf(
                            condition = index < attachmentsState.draftAttachmentsList.lastIndex,
                            ifTrue = { padding(bottom = Spacing.medium) }
                        )
                        .applyIf(
                            condition = isDetail && index == attachmentsState.draftAttachmentsList.lastIndex,
                            ifTrue = { padding(bottom = Spacing.medium) }
                        ),
                    filename = fileMetadata.name,
                    isEnabled = interactionsEnabled,
                    isLoading = attachmentsState.loadingDraftAttachments.contains(fileMetadata.uri),
                    isError = attachmentsState.errorDraftAttachments.contains(fileMetadata.uri),
                    attachmentType = fileMetadata.attachmentType,
                    size = fileMetadata.size,
                    createTime = fileMetadata.createTime,
                    onRetryClick = { onGuardedEvent(OnDraftAttachmentRetry(fileMetadata)) },
                    onOptionsClick = { onGuardedEvent(OnDraftAttachmentOptions(fileMetadata.uri)) },
                    onAttachmentOpen = {
                        onGuardedEvent(
                            OnDraftAttachmentOpen(
                                uri = fileMetadata.uri,
                                mimetype = fileMetadata.mimeType
                            )
                        )
                    }
                )
                if (index < attachmentsState.draftAttachmentsList.lastIndex) {
                    PassDivider()
                }
            }
        }
        if (!isDetail) {
            AddAttachmentButton(
                modifier = Modifier.padding(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.medium
                ),
                colors = itemColors,
                isEnabled = interactionsEnabled,
                onClick = {
                    when (val needsUpgrade = attachmentsState.needsUpgrade) {
                        None -> {}
                        is Some -> if (needsUpgrade.value) {
                            onGuardedEvent(AttachmentContentEvent.UpsellAttachments)
                        } else {
                            onGuardedEvent(OnAddAttachment)
                        }
                    }
                }
            )
        }
    }
}

class ThemeAttachmentSectionPreviewProvider :
    ThemePairPreviewProvider<Pair<Boolean, AttachmentsState>>(AttachmentSectionPreviewProvider())

@Preview
@Composable
fun AttachmentSectionPreview(
    @PreviewParameter(ThemeAttachmentSectionPreviewProvider::class)
    input: Pair<Boolean, Pair<Boolean, AttachmentsState>>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AttachmentSection(
                attachmentsState = input.second.second,
                isDetail = input.second.first,
                itemColors = passItemColors(itemCategory = ItemCategory.Login),
                onEvent = {}
            )
        }
    }
}
