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

package proton.android.pass.features.sl.sync.mailboxes.verify.presentation

import androidx.annotation.StringRes
import proton.android.pass.features.sl.sync.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class SimpleLoginSyncMailboxVerifyMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {

    VerifyCodeError(
        id = R.string.simple_login_sync_mailbox_verify_message_verification_code_error,
        type = SnackbarType.ERROR
    ),

    VerifyCodeLimitError(
        id = R.string.simple_login_sync_mailbox_verify_message_verification_code_limit_error,
        type = SnackbarType.ERROR
    ),

    VerifyMailboxError(
        id = R.string.simple_login_sync_mailbox_verify_message_verification_error,
        type = SnackbarType.ERROR
    ),

    VerifyMailboxSuccess(
        id = R.string.simple_login_sync_mailbox_verify_message_verification_success,
        type = SnackbarType.SUCCESS
    ),

    ResendCodeError(
        id = R.string.simple_login_sync_mailbox_verify_message_resend_code_error,
        type = SnackbarType.ERROR
    ),

    ResendCodeSuccess(
        id = R.string.simple_login_sync_mailbox_verify_message_resend_code_success,
        type = SnackbarType.SUCCESS
    )

}
