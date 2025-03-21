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

package proton.android.pass.features.itemcreate.alias

import androidx.annotation.StringRes
import proton.android.pass.features.itemcreate.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AliasSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    InitError(R.string.create_alias_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_alias_item_creation_error, SnackbarType.ERROR),
    CannotCreateMoreAliasesError(R.string.create_alias_cannot_create_more_aliases_error, SnackbarType.ERROR),
    EmailNotValidatedError(R.string.create_alias_email_not_validated_error, SnackbarType.ERROR),
    AliasRateLimited(R.string.alias_rate_limited, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_alias_item_update_error, SnackbarType.ERROR),
    AliasCreated(R.string.alias_created, SnackbarType.SUCCESS),
    AliasUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    CannotRetrieveAliasOptions(R.string.cannot_retrieve_alias_options, SnackbarType.ERROR),
    UpdateAppToUpdateItemError(R.string.snackbar_update_app_to_update_item, SnackbarType.ERROR),
    InitAttachmentsError(R.string.update_alias_attachments_init_error, SnackbarType.ERROR),
    ItemLinkAttachmentsError(R.string.alias_link_attachments_error, SnackbarType.ERROR),
    ItemRenameAttachmentsError(R.string.alias_rename_attachments_error, SnackbarType.ERROR)
}
