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

package proton.android.pass.features.attachments.attachmentoptionsonedit.presentation

import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import java.net.URI

sealed interface AttachmentOptionsOnEditEvent {

    data object Idle : AttachmentOptionsOnEditEvent

    data object Close : AttachmentOptionsOnEditEvent

    data class OpenRenameAttachment(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : AttachmentOptionsOnEditEvent

    @JvmInline
    value class OpenRenameDraftAttachment(val uri: URI) : AttachmentOptionsOnEditEvent

}
