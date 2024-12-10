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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentKey
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.FileMetadata
import java.net.URI

class AttachmentSectionPreviewProvider :
    PreviewParameterProvider<Pair<Boolean, AttachmentsState>> {

    override val values: Sequence<Pair<Boolean, AttachmentsState>>
        get() = sequence {
            for (isDetail in listOf(true, false)) {
                yield(
                    isDetail to createAttachmentsUiState(
                        attachments = listOf(
                            createFile(AttachmentId("1"), "file1", AttachmentType.Pdf),
                            createFile(AttachmentId("2"), "file2", AttachmentType.RasterImage)
                        )
                    )
                )
                yield(
                    isDetail to createAttachmentsUiState(
                        attachments = listOf(
                            createFile(AttachmentId("1"), "file1", AttachmentType.Calendar),
                            createFile(AttachmentId("2"), "file2", AttachmentType.Audio)
                        ),
                        loadingAttachments = setOf(AttachmentId("1"))
                    )
                )
                yield(
                    isDetail to createAttachmentsUiState(
                        attachments = emptyList()
                    )
                )
            }
        }

    @Suppress("MagicNumber")
    private fun createFile(
        id: AttachmentId,
        name: String,
        type: AttachmentType,
        size: Long = 1_572_864L, // 1.5 MB
        createTime: Instant = Instant.fromEpochSeconds(1_640_000_000L)
    ) = Attachment(
        id = id,
        name = name,
        type = type,
        size = size,
        createTime = createTime,
        mimeType = "",
        fileKey = AttachmentKey(""),
        itemKeyRotation = "",
        chunks = listOf()
    )

    private fun createAttachmentsUiState(
        attachments: List<Attachment>,
        draftAttachments: List<FileMetadata> = emptyList(),
        loadingAttachments: Set<AttachmentId> = emptySet(),
        loadingDraftAttachments: Set<URI> = emptySet()
    ) = AttachmentsState(
        draftAttachmentsList = draftAttachments,
        attachmentsList = attachments,
        loadingDraftAttachments = loadingDraftAttachments,
        loadingAttachments = loadingAttachments
    )
}
