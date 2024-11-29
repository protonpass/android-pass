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

package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.attachments.AttachmentRow
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.domain.attachments.Attachment

@Composable
internal fun CreateNoteItemForm(
    modifier: Modifier = Modifier,
    noteItemFormState: NoteItemFormState,
    isFileAttachmentsEnabled: Boolean,
    attachmentList: List<Attachment>,
    enabled: Boolean,
    onTitleRequiredError: Boolean,
    onEvent: (NoteContentUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        NoteTitle(
            value = noteItemFormState.title,
            requestFocus = true,
            onTitleRequiredError = onTitleRequiredError,
            enabled = enabled,
            onValueChanged = { onEvent(NoteContentUiEvent.OnTitleChange(it)) }
        )
        val shouldApplyWeight = remember(isFileAttachmentsEnabled, attachmentList) {
            !isFileAttachmentsEnabled || attachmentList.isEmpty()
        }
        FullNoteSection(
            modifier = Modifier
                .applyIf(shouldApplyWeight, ifTrue = { weight(1f) }),
            textFieldModifier = Modifier
                .applyIf(shouldApplyWeight, ifTrue = { weight(1f) })
                .fillMaxWidth(),
            enabled = enabled,
            value = noteItemFormState.note,
            onChange = { onEvent(NoteContentUiEvent.OnNoteChange(it)) }
        )
        if (isFileAttachmentsEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                attachmentList.forEach { attachment ->
                    AttachmentRow(
                        modifier = Modifier
                            .roundedContainerNorm()
                            .padding(
                                start = Spacing.medium,
                                top = Spacing.small,
                                end = Spacing.none,
                                bottom = Spacing.small
                            ),
                        filename = attachment.name,
                        attachmentType = attachment.type,
                        size = attachment.size,
                        createTime = attachment.createTime,
                        isLoading = false,
                        isEnabled = true,
                        onOptionsClick = {
                            onEvent(NoteContentUiEvent.OnAttachmentOptions(attachment.id))
                        },
                        onAttachmentOpen = {
                            onEvent(NoteContentUiEvent.OnAttachmentOpen(attachment.id))
                        }
                    )
                }
            }
        }
    }
}
