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

package proton.android.pass.features.itemcreate.creditcard

import androidx.annotation.StringRes
import proton.android.pass.features.itemcreate.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class CreditCardSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    ItemCreationError(R.string.create_credit_card_item_creation_error, SnackbarType.ERROR),
    ItemCreated(R.string.create_credit_card_item_creation_success, SnackbarType.SUCCESS),
    InitError(R.string.credit_card_init_error, SnackbarType.ERROR),
    ItemUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    ItemUpdateError(R.string.credit_card_item_update_error, SnackbarType.ERROR),
    UpdateAppToUpdateItemError(R.string.snackbar_update_app_to_update_item, SnackbarType.ERROR),
    AttachmentsInitError(R.string.update_credit_card_attachments_init_error, SnackbarType.ERROR),
    ItemLinkAttachmentsError(R.string.credit_card_link_attachments_error, SnackbarType.ERROR),
    ItemRenameAttachmentsError(R.string.credit_card_rename_attachments_error, SnackbarType.ERROR)

}
