/*
 * Copyright (c) 2025 Proton AG
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

import proton.android.pass.common.api.Option
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation
import java.net.URI

sealed interface CreateNoteNavigation {
    data object NoteCreated : CreateNoteNavigation
    data class SelectVault(val shareId: ShareId) : CreateNoteNavigation
}

sealed interface UpdateNoteNavigation {
    data class NoteUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateNoteNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : UpdateNoteNavigation
}

sealed interface BaseNoteNavigation {
    data class OnCreateNoteEvent(val event: CreateNoteNavigation) : BaseNoteNavigation
    data class OnUpdateNoteEvent(val event: UpdateNoteNavigation) : BaseNoteNavigation

    data object AddAttachment : BaseNoteNavigation
    data object UpsellAttachments : BaseNoteNavigation
    data object Upgrade : BaseNoteNavigation
    data object CloseScreen : BaseNoteNavigation
    data object DismissBottomsheet : BaseNoteNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : BaseNoteNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : BaseNoteNavigation

    @JvmInline
    value class NoteCustomFieldNavigation(val event: CustomFieldNavigation) : BaseNoteNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : BaseNoteNavigation
    data object TotpCancel : BaseNoteNavigation

    @JvmInline
    value class OpenImagePicker(val index: Option<Int>) : BaseNoteNavigation

    @JvmInline
    value class ScanTotp(val index: Option<Int>) : BaseNoteNavigation
}
