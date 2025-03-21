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

package proton.android.pass.features.security.center.addressoptions.presentation

import androidx.annotation.StringRes
import proton.android.pass.features.security.center.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class SecurityCenterAddressOptionsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {

    EnableMonitoringError(
        id = R.string.security_center_address_bottomsheet_snackbar_message_enable_error,
        type = SnackbarType.ERROR
    ),

    DisableMonitoringError(
        id = R.string.security_center_address_bottomsheet_snackbar_message_disable_error,
        type = SnackbarType.ERROR
    ),

    RemoveCustomEmailError(
        id = R.string.security_center_address_bottomsheet_snackbar_message_remove_custom_email_error,
        type = SnackbarType.ERROR
    ),

    EnableMonitoringSuccess(
        id = R.string.security_center_address_bottomsheet_snackbar_message_enable_success,
        type = SnackbarType.SUCCESS
    ),

    DisableMonitoringSuccess(
        id = R.string.security_center_address_bottomsheet_snackbar_message_disable_success,
        type = SnackbarType.SUCCESS
    ),

    RemoveCustomEmailSuccess(
        id = R.string.security_center_address_bottomsheet_snackbar_message_remove_custom_email_success,
        type = SnackbarType.SUCCESS
    )
}
