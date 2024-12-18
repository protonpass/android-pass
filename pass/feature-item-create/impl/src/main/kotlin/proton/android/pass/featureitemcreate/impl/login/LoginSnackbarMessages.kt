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

package proton.android.pass.featureitemcreate.impl.login

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class LoginSnackbarMessages(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    InitError(R.string.create_login_init_error, SnackbarType.ERROR),
    AttachmentsInitError(R.string.update_note_attachments_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_login_item_creation_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_login_item_update_error, SnackbarType.ERROR),
    LoginCreated(R.string.login_created, SnackbarType.SUCCESS),
    LoginUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    CannotCreateMoreAliases(R.string.create_alias_cannot_create_more_aliases_error, SnackbarType.ERROR),
    EmailNotValidated(R.string.create_alias_email_not_validated_error, SnackbarType.ERROR),
    AliasRateLimited(R.string.alias_rate_limited, SnackbarType.ERROR),
    InvalidTotpError(R.string.create_login_invalid_totp, SnackbarType.ERROR),
    UpdateAppToUpdateItemError(R.string.snackbar_update_app_to_update_item, SnackbarType.ERROR)
}
