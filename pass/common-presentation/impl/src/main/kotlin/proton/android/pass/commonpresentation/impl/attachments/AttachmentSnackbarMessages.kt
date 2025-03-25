/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.commonpresentation.impl.attachments

import androidx.annotation.StringRes
import proton.android.pass.commonpresentation.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AttachmentSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    OpenAttachmentsError(R.string.open_attachments_error, SnackbarType.ERROR),
    AttachmentSizeExceededError(R.string.attachment_size_exceeded_error, SnackbarType.ERROR),
    UploadAttachmentsError(R.string.upload_attachments_error, SnackbarType.ERROR)
}
