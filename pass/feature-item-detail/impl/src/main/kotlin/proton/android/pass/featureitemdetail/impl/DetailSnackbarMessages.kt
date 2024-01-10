/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemdetail.impl

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class DetailSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    InitError(R.string.detail_init_error, SnackbarType.ERROR),
    AliasCopiedToClipboard(R.string.alias_copied_to_clipboard, SnackbarType.NORM, true),
    UsernameCopiedToClipboard(R.string.username_copied_to_clipboard, SnackbarType.NORM, true),
    PasswordCopiedToClipboard(R.string.password_copied_to_clipboard, SnackbarType.NORM, true),
    WebsiteCopiedToClipboard(R.string.website_copied_to_clipboard, SnackbarType.NORM, true),
    TotpCopiedToClipboard(R.string.totp_copied_to_clipboard, SnackbarType.NORM, true),
    NoteCopiedToClipboard(R.string.note_copied_to_clipboard, SnackbarType.NORM, true),
    CardHolderCopiedToClipboard(R.string.credit_card_cardholder_copied_to_clipboard, SnackbarType.NORM, true),
    CardNumberCopiedToClipboard(R.string.credit_card_number_copied_to_clipboard, SnackbarType.NORM, true),
    CardVerificationNumberCopiedToClipboard(
        id = R.string.credit_card_verifcation_number_copied_to_clipboard,
        type = SnackbarType.NORM,
        isClipboard = true
    ),
    FieldCopiedToClipboard(R.string.field_copied_to_clipboard, SnackbarType.NORM, true),
    ItemMovedToTrash(R.string.move_item_to_trash_message, SnackbarType.NORM),
    ItemNotMovedToTrash(R.string.error_move_item_to_trash_message, SnackbarType.ERROR),
    ItemPermanentlyDeleted(R.string.item_permanently_deleted_message, SnackbarType.NORM),
    ItemNotPermanentlyDeleted(R.string.item_permanently_deleted_message_error, SnackbarType.ERROR),
    ItemRestored(R.string.item_restored_message, SnackbarType.NORM),
    ItemNotRestored(R.string.item_restored_message_error, SnackbarType.ERROR),
    GenerateTotpError(R.string.login_item_generate_totp_error, SnackbarType.ERROR),
    ItemPinnedSuccess(R.string.item_pinned_message, SnackbarType.SUCCESS),
    ItemPinnedError(R.string.item_pinned_message_error, SnackbarType.ERROR),
    ItemUnpinnedSuccess(R.string.item_unpinned_message, SnackbarType.SUCCESS),
    ItemUnpinnedError(R.string.item_unpinned_message_error, SnackbarType.ERROR),
}
