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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.common.api.None
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.DraftAttachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.attachments.PendingAttachmentId
import proton.android.pass.domain.attachments.PersistentAttachmentId
import java.net.URI

private const val SIZE_1_MB = 1_048_576L
private const val CREATE_TIME = 1_640_000_000L

class AttachmentSectionPreviewProvider :
    PreviewParameterProvider<Pair<Boolean, AttachmentsState>> {

    override val values: Sequence<Pair<Boolean, AttachmentsState>>
        get() = sequence {
            // only attachments
            yield(
                false to createAttachmentsUiState(
                    attachments = listOf(
                        createAttachment(AttachmentId("1"), "create-file1", AttachmentType.Pdf),
                        createAttachment(AttachmentId("2"), "create-file2", AttachmentType.RasterImage)
                    ),
                    draftAttachments = emptyList()
                )
            )
            // attachments and draft attachments for detail and non-detail view
            for (isDetail in listOf(true, false)) {
                val prefix = if (isDetail) "detail" else "create"
                yield(
                    isDetail to createAttachmentsUiState(
                        attachments = listOf(
                            createAttachment(AttachmentId("1"), "$prefix-file1", AttachmentType.Calendar),
                            createAttachment(AttachmentId("2"), "$prefix-file2", AttachmentType.Audio)
                        ),
                        draftAttachments = listOf(
                            createSuccessDraftAttachment(URI("file:///file1"), "draft-file1"),
                            createSuccessDraftAttachment(URI("file:///file2"), "draft-file2")
                        )
                    )
                )
            }
            // loading attachments
            yield(
                false to createAttachmentsUiState(
                    attachments = listOf(
                        createAttachment(AttachmentId("1"), "create-file1", AttachmentType.Calendar),
                        createAttachment(AttachmentId("2"), "create-file2", AttachmentType.Audio)
                    ),
                    loadingAttachments = setOf(AttachmentId("1")),
                    draftAttachments = emptyList()
                )
            )
            // loading draft attachments
            yield(
                false to createAttachmentsUiState(
                    draftAttachments = listOf(
                        createLoadingDraftAttachment(URI("file:///file1"), "draft-file1"),
                        createSuccessDraftAttachment(URI("file:///file2"), "draft-file2")
                    )
                )
            )
            // empty attachments for detail and non-detail view
            for (isDetail in listOf(true, false)) {
                yield(
                    isDetail to createAttachmentsUiState(
                        attachments = emptyList(),
                        draftAttachments = emptyList()
                    )
                )
            }
        }

    @Suppress("MagicNumber")
    private fun createAttachment(
        id: AttachmentId,
        name: String,
        type: AttachmentType,
        size: Long = SIZE_1_MB,
        createTime: Instant = Instant.fromEpochSeconds(CREATE_TIME),
        modifiedTime: Instant = Instant.fromEpochSeconds(CREATE_TIME)
    ) = Attachment(
        id = id,
        persistentId = PersistentAttachmentId("persistent-$id"),
        shareId = ShareId("share-$id"),
        itemId = ItemId("item-$id"),
        name = name,
        type = type,
        size = size,
        createTime = createTime,
        modifyTime = modifiedTime,
        revisionAdded = 1,
        revisionRemoved = null,
        mimeType = "",
        reencryptedKey = EncryptedByteArray(byteArrayOf()),
        chunks = listOf(),
        encryptionVersion = 1
    )

    private fun createSuccessDraftAttachment(
        uri: URI,
        name: String,
        size: Long = SIZE_1_MB,
        mimeType: String = "application/pdf",
        attachmentType: AttachmentType = AttachmentType.Pdf,
        createTime: Instant = Instant.fromEpochSeconds(CREATE_TIME)
    ) = DraftAttachment.Success(
        metadata = createFileMetadata(uri, name, size, mimeType, attachmentType, createTime),
        pendingAttachmentId = PendingAttachmentId("attachment-$name")
    )

    private fun createLoadingDraftAttachment(
        uri: URI,
        name: String,
        size: Long = SIZE_1_MB,
        mimeType: String = "application/pdf",
        attachmentType: AttachmentType = AttachmentType.Pdf,
        createTime: Instant = Instant.fromEpochSeconds(CREATE_TIME)
    ) = DraftAttachment.Loading(
        metadata = createFileMetadata(uri, name, size, mimeType, attachmentType, createTime)
    )

    private fun createFileMetadata(
        uri: URI,
        name: String,
        size: Long = SIZE_1_MB,
        mimeType: String = "application/pdf",
        attachmentType: AttachmentType = AttachmentType.Pdf,
        createTime: Instant = Instant.fromEpochSeconds(CREATE_TIME)
    ) = FileMetadata(
        uri = uri,
        name = name,
        size = size,
        mimeType = mimeType,
        attachmentType = attachmentType,
        createTime = createTime
    )

    private fun createAttachmentsUiState(
        attachments: List<Attachment> = emptyList(),
        draftAttachments: List<DraftAttachment> = emptyList(),
        loadingAttachments: Set<AttachmentId> = emptySet()
    ) = AttachmentsState(
        draftAttachmentsList = draftAttachments,
        attachmentsList = attachments,
        loadingAttachments = loadingAttachments,
        needsUpgrade = None,
        canUpgrade = false
    )
}
