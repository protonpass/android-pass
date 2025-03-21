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

package proton.android.pass.features.profile

import androidx.annotation.StringRes
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class ProfileSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage.StructuredMessage {
    BiometryFailedToStartError(R.string.profile_error_biometry_failed_to_start, SnackbarType.ERROR),
    BiometryFailedToAuthenticateError(R.string.profile_error_biometry_failed_to_authenticate, SnackbarType.ERROR),
    FingerprintLockEnabled(R.string.profile_fingerprint_lock_enabled, SnackbarType.SUCCESS),
    FingerprintLockDisabled(R.string.profile_fingerprint_lock_disabled, SnackbarType.SUCCESS),
    AppVersionCopied(R.string.profile_app_version_copied_to_clipboard, SnackbarType.SUCCESS, true),
    PinLockEnabled(R.string.configure_pin_pin_lock_enabled, SnackbarType.SUCCESS),
    PinLockDisabled(R.string.configure_pin_pin_lock_disabled, SnackbarType.SUCCESS),
    FilteredByLogins(R.string.filtered_by_logins, SnackbarType.NORM),
    FilteredByLoginsWithMFA(R.string.filtered_by_logins_with_mfa, SnackbarType.NORM),
    FilteredByAliases(R.string.filtered_by_aliases, SnackbarType.NORM),
    FilteredByNote(R.string.filtered_by_notes, SnackbarType.NORM),
    FilteredByCreditCards(R.string.filtered_by_credit_cards, SnackbarType.NORM),
    FilteredByIdentities(R.string.filtered_by_identities, SnackbarType.NORM),
    FilteredByCustomItems(R.string.filtered_by_custom_items, SnackbarType.NORM)
}
