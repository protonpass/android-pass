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

package proton.android.pass.features.itemcreate.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnAttachmentOpen
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnAttachmentOptions
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentOpen
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentOptions
import proton.android.pass.composecomponents.impl.attachments.AttachmentContentEvent.OnDraftAttachmentRetry
import proton.android.pass.composecomponents.impl.attachments.AttachmentRow
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.features.itemcreate.note.NoteContentUiEvent.OnAttachmentEvent

@Composable
fun AttachmentList(
    modifier: Modifier = Modifier,
    attachmentsState: AttachmentsState,
    onEvent: (NoteContentUiEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        attachmentsState.attachmentsList.forEach { attachment ->
            AttachmentRow(
                modifier = Modifier.roundedContainerNorm(),
                innerModifier = Modifier.padding(
                    start = Spacing.medium,
                    top = Spacing.small,
                    end = Spacing.none,
                    bottom = Spacing.small
                ),
                filename = attachment.name,
                attachmentType = attachment.type,
                size = attachment.size,
                createTime = attachment.createTime,
                isLoading = attachmentsState.loadingAttachments.contains(attachment.id),
                isEnabled = attachmentsState.isEnabled,
                isError = false,
                hasOptions = true,
                onOptionsClick = {
                    onEvent(
                        OnAttachmentEvent(
                            OnAttachmentOptions(
                                shareId = attachment.shareId,
                                itemId = attachment.itemId,
                                attachmentId = attachment.id
                            )
                        )
                    )
                },
                onAttachmentOpen = {
                    onEvent(OnAttachmentEvent(OnAttachmentOpen(attachment)))
                },
                onRetryClick = {}
            )
        }
        attachmentsState.draftAttachmentsList.forEach { draftAttachment ->
            val metadata = draftAttachment.metadata
            AttachmentRow(
                modifier = Modifier.roundedContainerNorm(),
                innerModifier = Modifier.padding(
                    start = Spacing.medium,
                    top = Spacing.small,
                    end = Spacing.none,
                    bottom = Spacing.small
                ),
                filename = metadata.name,
                attachmentType = metadata.attachmentType,
                hasOptions = true,
                size = metadata.size,
                createTime = metadata.createTime,
                isLoading = attachmentsState.loadingDraftAttachments.contains(metadata.uri),
                isEnabled = attachmentsState.isEnabled,
                isError = attachmentsState.errorDraftAttachments.contains(metadata.uri),
                onOptionsClick = {
                    onEvent(OnAttachmentEvent(OnDraftAttachmentOptions(metadata.uri)))
                },
                onAttachmentOpen = {
                    onEvent(
                        OnAttachmentEvent(
                            OnDraftAttachmentOpen(
                                uri = metadata.uri,
                                mimetype = metadata.mimeType
                            )
                        )
                    )
                },
                onRetryClick = {
                    onEvent(OnAttachmentEvent(OnDraftAttachmentRetry(metadata)))
                }
            )
        }
    }
}
