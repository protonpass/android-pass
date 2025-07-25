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

package proton.android.pass.features.item.history.restore.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.domain.attachments.AttachmentId

@Stable
internal sealed interface ItemHistoryRestoreState {

    @Stable
    data object Initial : ItemHistoryRestoreState

    @Stable
    data class ItemDetails(
        internal val currentItemDetailState: ItemDetailState,
        internal val revisionItemDetailState: ItemDetailState,
        internal val itemRevision: ItemRevision,
        internal val event: ItemHistoryRestoreEvent = ItemHistoryRestoreEvent.Idle,
        internal val isFileAttachmentEnabled: Boolean,
        internal val isCustomItemEnabled: Boolean
    ) : ItemHistoryRestoreState {

        private val currentAttachmentIdMap = currentItemDetailState.attachmentsState.attachmentsList
            .associateBy { it.persistentId }
        private val revisionAttachmentIdMap = revisionItemDetailState.attachmentsState.attachmentsList
            .associateBy { it.persistentId }

        val attachmentsToRestore: Set<AttachmentId> = revisionItemDetailState.attachmentsState.attachmentsList
            .filter { it.persistentId !in currentAttachmentIdMap }
            .map { it.id }
            .toSet()

        val attachmentsToDelete: Set<AttachmentId> = currentItemDetailState.attachmentsState.attachmentsList
            .filter { it.persistentId !in revisionAttachmentIdMap }
            .map { it.id }
            .toSet()

    }

}
