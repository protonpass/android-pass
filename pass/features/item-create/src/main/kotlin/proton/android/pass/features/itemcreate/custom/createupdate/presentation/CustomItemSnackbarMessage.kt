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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import androidx.annotation.StringRes
import proton.android.pass.features.itemcreate.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class CustomItemSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    InitError(R.string.create_custom_item_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_custom_item_item_creation_error, SnackbarType.ERROR),
    ItemCreated(R.string.create_custom_item_item_creation_success, SnackbarType.SUCCESS),
    ItemUpdateError(R.string.update_custom_item_item_creation_error, SnackbarType.ERROR),
    ItemUpdated(R.string.update_custom_item_item_creation_success, SnackbarType.SUCCESS),
    AttachmentsInitError(R.string.update_custom_item_attachments_init_error, SnackbarType.ERROR),
    ItemLinkAttachmentsError(R.string.custom_item_link_attachments_error, SnackbarType.ERROR),
    ItemRenameAttachmentsError(R.string.custom_item_rename_attachments_error, SnackbarType.ERROR)
}
