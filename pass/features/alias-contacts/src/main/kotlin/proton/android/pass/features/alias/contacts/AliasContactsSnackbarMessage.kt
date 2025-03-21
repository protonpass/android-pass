/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.alias.contacts

import androidx.annotation.StringRes
import proton.android.pass.features.aliascontacts.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AliasContactsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    ContactCreateSuccess(R.string.snackbar_contact_create_success, SnackbarType.SUCCESS),
    ContactCreateError(R.string.snackbar_contact_create_error, SnackbarType.ERROR),
    ContactBlockSuccess(R.string.snackbar_contact_block_success, SnackbarType.SUCCESS),
    ContactBlockError(R.string.snackbar_contact_block_error, SnackbarType.ERROR),
    ContactUnblockSuccess(R.string.snackbar_contact_unblock_success, SnackbarType.SUCCESS),
    ContactUnblockError(R.string.snackbar_contact_unblock_error, SnackbarType.ERROR),
    EmailCopiedToClipboard(R.string.snackbar_email_copied_success, SnackbarType.NORM),
    DeleteContactSuccess(R.string.snackbar_delete_contact_success, SnackbarType.SUCCESS),
    DeleteContactError(R.string.snackbar_delete_contact_error, SnackbarType.ERROR)
}
