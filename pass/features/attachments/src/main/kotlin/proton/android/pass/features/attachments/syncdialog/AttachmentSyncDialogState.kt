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

import androidx.compose.runtime.Stable
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus

@Stable
internal data class AttachmentSyncDialogState(
    val status: SyncStatus,
    val downloaded: Int,
    val total: Int,
    val vaults: List<VaultSyncState>
) {

    internal enum class SyncStatus {
        Loading,
        WaitingForConnection,
        Downloading,
        Done,
        PartialFailure
    }

    internal companion object {

        internal val Initial = AttachmentSyncDialogState(
            status = SyncStatus.Loading,
            downloaded = 0,
            total = 0,
            vaults = emptyList()
        )

        internal val Empty = AttachmentSyncDialogState(
            status = SyncStatus.Done,
            downloaded = 0,
            total = 0,
            vaults = emptyList()
        )

    }

}

@Stable
internal data class VaultSyncState(
    val shareId: ShareId,
    val name: String,
    val color: ShareColor,
    val icon: ShareIcon,
    val items: List<ItemSyncState>,
    val totalSizeBytes: Long? = null
) {

    val downloadedCount: Int get() = items.sumOf { item ->
        item.attachments.count { it.status == AttachmentDownloadStatus.Downloaded }
    }

    val totalCount: Int get() = items.sumOf { it.attachments.size }

    val allDone: Boolean get() = totalCount > 0 && downloadedCount >= totalCount

    val hasFailed: Boolean get() = items.any { it.hasFailed }

    val hasPending: Boolean get() = items.any { it.hasPending }

}

@Stable
internal data class ItemSyncState(
    val itemId: ItemId,
    val shareId: ShareId,
    val name: String,
    val itemCategory: ItemCategory,
    val attachments: List<AttachmentSyncRowState>
) {

    val allDone: Boolean get() = attachments.all { it.status == AttachmentDownloadStatus.Downloaded }

    val hasFailed: Boolean get() = attachments.any { it.status == AttachmentDownloadStatus.Failed }

    val hasPending: Boolean get() = attachments.any {
        it.status == AttachmentDownloadStatus.Pending ||
            it.status == AttachmentDownloadStatus.Downloading ||
            it.status == AttachmentDownloadStatus.Idle ||
            it.status == AttachmentDownloadStatus.Paused
    }

}

@Stable
internal data class AttachmentSyncRowState(
    val attachment: Attachment,
    val name: String,
    val size: Long,
    val status: AttachmentDownloadStatus
)
