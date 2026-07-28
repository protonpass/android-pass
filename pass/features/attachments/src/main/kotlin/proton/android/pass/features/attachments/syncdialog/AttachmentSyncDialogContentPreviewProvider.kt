/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.attachments.syncdialog

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.PersistentAttachmentId
import proton.android.pass.features.attachments.syncdialog.AttachmentSyncDialogState.SyncStatus

internal class AttachmentSyncDialogContentPreviewProvider :
    PreviewParameterProvider<Pair<Boolean, AttachmentSyncDialogState>> {

    private val states: Sequence<AttachmentSyncDialogState> = sequenceOf(
        // Waiting for connection
        AttachmentSyncDialogState(
            status = SyncStatus.WaitingForConnection,
            downloaded = 0,
            total = 5,
            vaults = previewVaults()
        ),
        // Downloading (partial progress)
        AttachmentSyncDialogState(
            status = SyncStatus.Downloading,
            downloaded = 3,
            total = 10,
            vaults = previewVaults()
        ),
        // All done
        AttachmentSyncDialogState(
            status = SyncStatus.Done,
            downloaded = 10,
            total = 10,
            vaults = listOf(
                VaultSyncState(
                    shareId = ShareId("share-1"),
                    name = "Personal",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    items = listOf(
                        ItemSyncState(
                            itemId = ItemId("item-1"),
                            shareId = ShareId("share-1"),
                            name = "My Login",
                            itemCategory = ItemCategory.Login,
                            attachments = listOf(
                                previewAttachmentRow(
                                    id = "1",
                                    name = "document.pdf",
                                    size = PREVIEW_SIZE_1MB,
                                    status = AttachmentDownloadStatus.Downloaded
                                )
                            )
                        )
                    )
                )
            )
        ),
        // Partial failure with retry
        AttachmentSyncDialogState(
            status = SyncStatus.PartialFailure,
            downloaded = 8,
            total = 10,
            vaults = listOf(
                VaultSyncState(
                    shareId = ShareId("share-1"),
                    name = "Personal",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    items = listOf(
                        ItemSyncState(
                            itemId = ItemId("item-1"),
                            shareId = ShareId("share-1"),
                            name = "My Login",
                            itemCategory = ItemCategory.Login,
                            attachments = listOf(
                                previewAttachmentRow(
                                    id = "1",
                                    name = "document.pdf",
                                    size = PREVIEW_SIZE_1MB,
                                    status = AttachmentDownloadStatus.Failed
                                ),
                                previewAttachmentRow(
                                    id = "2",
                                    name = "photo.jpg",
                                    size = PREVIEW_SIZE_2MB,
                                    status = AttachmentDownloadStatus.Failed
                                )
                            )
                        )
                    )
                ),
                VaultSyncState(
                    shareId = ShareId("share-2"),
                    name = "Work",
                    color = ShareColor.Color2,
                    icon = ShareIcon.Icon2,
                    items = listOf(
                        ItemSyncState(
                            itemId = ItemId("item-2"),
                            shareId = ShareId("share-2"),
                            name = "Work Account",
                            itemCategory = ItemCategory.Note,
                            attachments = listOf(
                                previewAttachmentRow(
                                    id = "3",
                                    name = "report.xlsx",
                                    size = PREVIEW_SIZE_512KB,
                                    status = AttachmentDownloadStatus.Downloaded
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    override val values: Sequence<Pair<Boolean, AttachmentSyncDialogState>> =
        states.flatMap { state ->
            sequenceOf(false to state, true to state)
        }

    private fun previewVaults() = listOf(
        VaultSyncState(
            shareId = ShareId("share-1"),
            name = "Personal",
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1,
            items = listOf(
                ItemSyncState(
                    itemId = ItemId("item-1"),
                    shareId = ShareId("share-1"),
                    name = "My Login",
                    itemCategory = ItemCategory.Login,
                    attachments = listOf(
                        previewAttachmentRow(
                            id = "1",
                            name = "document.pdf",
                            size = PREVIEW_SIZE_1MB,
                            status = AttachmentDownloadStatus.Pending
                        ),
                        previewAttachmentRow(
                            id = "2",
                            name = "photo.jpg",
                            size = PREVIEW_SIZE_2MB,
                            status = AttachmentDownloadStatus.Downloaded
                        )
                    )
                )
            )
        ),
        VaultSyncState(
            shareId = ShareId("share-2"),
            name = "Work",
            color = ShareColor.Color2,
            icon = ShareIcon.Icon2,
            items = listOf(
                ItemSyncState(
                    itemId = ItemId("item-2"),
                    shareId = ShareId("share-2"),
                    name = "Work Account",
                    itemCategory = ItemCategory.Note,
                    attachments = listOf(
                        previewAttachmentRow(
                            id = "3",
                            name = "report.xlsx",
                            size = PREVIEW_SIZE_512KB,
                            status = AttachmentDownloadStatus.Pending
                        )
                    )
                )
            )
        )
    )

    @Suppress("LongParameterList")
    private fun previewAttachmentRow(
        id: String,
        name: String,
        size: Long,
        status: AttachmentDownloadStatus
    ) = AttachmentSyncRowState(
        attachment = previewAttachment(id, name, size, status),
        name = name,
        size = size,
        status = status
    )

    @Suppress("LongParameterList")
    private fun previewAttachment(
        id: String,
        name: String,
        size: Long,
        status: AttachmentDownloadStatus
    ) = Attachment(
        id = AttachmentId(id),
        persistentId = PersistentAttachmentId(id),
        shareId = ShareId("share-1"),
        itemId = ItemId("item-1"),
        name = name,
        mimeType = "application/octet-stream",
        type = AttachmentType.Unknown,
        size = size,
        createTime = Instant.DISTANT_PAST,
        modifyTime = Instant.DISTANT_PAST,
        revisionAdded = 1,
        revisionRemoved = null,
        reencryptedKey = EncryptedByteArray(byteArrayOf()),
        chunks = emptyList(),
        encryptionVersion = 1,
        downloadStatus = status
    )

    private companion object {

        private const val PREVIEW_SIZE_512KB = 512_000L
        private const val PREVIEW_SIZE_1MB = 1_048_576L
        private const val PREVIEW_SIZE_2MB = 2_097_152L

    }

}
