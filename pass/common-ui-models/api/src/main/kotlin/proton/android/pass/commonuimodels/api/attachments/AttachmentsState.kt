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

package proton.android.pass.commonuimodels.api.attachments

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.getOrElse
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.DraftAttachment
import java.net.URI

@Immutable
data class AttachmentsState(
    val draftAttachmentsList: List<DraftAttachment>,
    val attachmentsList: List<Attachment>,
    val loadingAttachments: Set<AttachmentId>,
    val needsUpgrade: Option<Boolean>,
    val canUpgrade: Boolean
) {

    val hasAnyAttachment: Boolean
        get() = draftAttachmentsList.isNotEmpty() || attachmentsList.isNotEmpty()

    val loadingDraftAttachments: Set<URI>
        get() = draftAttachmentsList.filterIsInstance<DraftAttachment.Loading>()
            .map { it.metadata.uri }
            .toSet()
    val errorDraftAttachments: Set<URI>
        get() = draftAttachmentsList.filterIsInstance<DraftAttachment.Error>()
            .map { it.metadata.uri }
            .toSet()

    private val isAnyAttachmentLoading: Boolean
        get() = loadingDraftAttachments.isNotEmpty() || loadingAttachments.isNotEmpty()

    val isEnabled = !isAnyAttachmentLoading

    val size = draftAttachmentsList.size + attachmentsList.size

    val allToUnlink: Set<AttachmentId>
        get() = attachmentsList.map { it.id }.toSet()

    fun canShowAttachmentSection(isDetail: Boolean): Boolean = if (isDetail) {
        hasAnyAttachment
    } else when {
        canUpgrade && needsUpgrade.getOrElse { false } || // Free account
            !canUpgrade && !needsUpgrade.getOrElse { false } // Unlimited account
        -> true

        else -> false // Pass essentials account
    }

    fun shouldDisplayDivider(index: Int): Boolean =
        index < attachmentsList.lastIndex || draftAttachmentsList.isNotEmpty()

    companion object {
        val Initial = AttachmentsState(
            draftAttachmentsList = emptyList(),
            attachmentsList = emptyList(),
            loadingAttachments = emptySet(),
            needsUpgrade = None,
            canUpgrade = false
        )
    }
}
