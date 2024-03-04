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

package proton.android.pass.commonpresentation.impl.items.details.messages

import androidx.annotation.StringRes
import proton.android.pass.commonpresentation.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class ItemDetailsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false,
) : SnackbarMessage {

    AliasCopied(
        id = R.string.item_details_snackbar_message_alias_copied,
        type = SnackbarType.NORM,
    ),

    CardNumberCopied(
        id = R.string.item_details_snackbar_message_card_number_copied,
        type = SnackbarType.NORM,
    ),

    CustomFieldCopied(
        id = R.string.item_details_snackbar_message_custom_field_copied,
        type = SnackbarType.NORM,
    ),

    CvvCopied(
        id = R.string.item_details_snackbar_message_cvv_copied,
        type = SnackbarType.NORM,
    ),

    PasswordCopied(
        id = R.string.item_details_snackbar_message_password_copied,
        type = SnackbarType.NORM,
    ),

    PinCopied(
        id = R.string.item_details_snackbar_message_pin_copied,
        type = SnackbarType.NORM,
    ),

    TotpCodeCopied(
        id = R.string.item_details_snackbar_message_totp_copied,
        type = SnackbarType.NORM,
    ),

    UsernameCopied(
        id = R.string.item_details_snackbar_message_username_copied,
        type = SnackbarType.NORM,
    ),

    WebsiteCopied(
        id = R.string.item_details_snackbar_message_website_copied,
        type = SnackbarType.NORM,
    ),

}
