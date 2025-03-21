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

package proton.android.pass.features.vault

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class VaultSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    EditVaultSuccess(R.string.snackbar_edit_vault_success, SnackbarType.SUCCESS),
    EditVaultError(R.string.snackbar_edit_vault_error, SnackbarType.ERROR),
    DeleteVaultSuccess(R.string.snackbar_delete_vault_success, SnackbarType.SUCCESS),
    DeleteVaultError(R.string.snackbar_delete_vault_error, SnackbarType.ERROR),
    LeaveVaultSuccess(R.string.snackbar_leave_vault_success, SnackbarType.SUCCESS),
    LeaveVaultError(R.string.snackbar_leave_vault_error, SnackbarType.ERROR),
    CreateVaultSuccess(R.string.snackbar_create_vault_success, SnackbarType.SUCCESS),
    CannotCreateMoreVaultsError(
        R.string.snackbar_cannot_create_more_vaults_error,
        SnackbarType.ERROR
    ),
    CreateVaultError(R.string.snackbar_create_vault_error, SnackbarType.ERROR),
    CannotRetrieveVaultError(R.string.snackbar_retrieve_vault_error, SnackbarType.ERROR),
    CannotFindVaultError(R.string.snackbar_find_vault_error, SnackbarType.ERROR),
    CannotGetVaultListError(R.string.snackbar_get_vault_list_error, SnackbarType.ERROR)
}
