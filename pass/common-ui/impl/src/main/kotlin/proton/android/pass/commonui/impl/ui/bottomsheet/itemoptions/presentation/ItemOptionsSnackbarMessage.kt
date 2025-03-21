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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.presentation

import androidx.annotation.StringRes
import proton.android.pass.commonui.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class ItemOptionsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    SentToTrashSuccess(
        id = R.string.snackbar_item_move_to_trash_success,
        type = SnackbarType.SUCCESS
    ),

    SentToTrashError(
        id = R.string.snackbar_item_move_to_trash_error,
        type = SnackbarType.ERROR
    ),

    EmailCopiedToClipboard(
        id = R.string.snackbar_item_copy_email_success,
        type = SnackbarType.SUCCESS,
        isClipboard = true
    ),

    UsernameCopiedToClipboard(
        id = R.string.snackbar_item_copy_username_success,
        type = SnackbarType.SUCCESS,
        isClipboard = true
    ),

    PasswordCopiedToClipboard(
        id = R.string.snackbar_item_copy_password_success,
        type = SnackbarType.SUCCESS,
        isClipboard = true
    )
}
