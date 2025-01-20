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

package proton.android.pass.features.sharing

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class SharingSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    InviteAccepted(R.string.sharing_snackbar_invite_accepted, SnackbarType.SUCCESS),
    InviteAcceptError(R.string.sharing_snackbar_invite_accept_error, SnackbarType.ERROR),
    InviteAcceptErrorCannotCreateMoreVaults(
        id = R.string.sharing_snackbar_invite_accept_error_cannot_create_more_vaults,
        type = SnackbarType.ERROR
    ),
    InviteRejected(R.string.sharing_snackbar_invite_rejected, SnackbarType.SUCCESS),
    InviteRejectError(R.string.sharing_snackbar_invite_reject_error, SnackbarType.ERROR),
    InviteSentSuccess(R.string.sharing_snackbar_invite_sent_success, SnackbarType.SUCCESS),
    InviteSentError(R.string.sharing_snackbar_invite_sent_error, SnackbarType.ERROR),
    GetMembersInfoError(R.string.sharing_snackbar_get_vault_members_error, SnackbarType.ERROR),
    ChangeMemberPermissionSuccess(
        R.string.sharing_snackbar_change_permission_success,
        SnackbarType.NORM
    ),
    ChangeMemberPermissionError(
        R.string.sharing_snackbar_change_permission_error,
        SnackbarType.ERROR
    ),
    RemoveMemberSuccess(R.string.sharing_snackbar_remove_member_success, SnackbarType.NORM),
    RemoveMemberError(R.string.sharing_snackbar_remove_member_error, SnackbarType.ERROR),

    CancelInviteSuccess(R.string.sharing_snackbar_cancel_invite_success, SnackbarType.NORM),
    CancelInviteError(R.string.sharing_snackbar_cancel_invite_error, SnackbarType.ERROR),
    ResendInviteSuccess(R.string.sharing_snackbar_resend_invite_success, SnackbarType.NORM),
    ResendInviteError(R.string.sharing_snackbar_resend_invite_error, SnackbarType.ERROR),
    TooManyInvitesSentError(R.string.sharing_snackbar_too_many_invites_sent, SnackbarType.ERROR),

    ConfirmInviteSuccess(R.string.sharing_snackbar_confirm_invite_success, SnackbarType.NORM),
    ConfirmInviteError(R.string.sharing_snackbar_confirm_invite_error, SnackbarType.ERROR),

    TransferOwnershipSuccess(
        R.string.sharing_snackbar_transfer_ownership_success,
        SnackbarType.NORM
    ),
    TransferOwnershipError(R.string.sharing_snackbar_transfer_ownership_error, SnackbarType.ERROR),

    FetchMembersError(
        id = R.string.sharing_snackbar_fetch_member_error,
        type = SnackbarType.ERROR
    ),
    FetchPendingInvitesError(
        id = R.string.sharing_snackbar_fetch_pending_invites_error,
        type = SnackbarType.ERROR
    ),
    NewUsersInviteError(
        id = R.string.sharing_snackbar_invite_new_users_error,
        type = SnackbarType.ERROR
    )
}
