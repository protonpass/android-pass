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

package proton.android.pass.biometry

sealed interface BiometryAuthError {
    data object HardwareUnavailable : BiometryAuthError
    data object UnableToProcess : BiometryAuthError
    data object Timeout : BiometryAuthError
    data object NoSpace : BiometryAuthError
    data object Canceled : BiometryAuthError
    data object Lockout : BiometryAuthError
    data object Vendor : BiometryAuthError
    data object LockoutPermanent : BiometryAuthError
    data object UserCanceled : BiometryAuthError
    data object NoBiometrics : BiometryAuthError
    data object HardwareNotPresent : BiometryAuthError
    data object NegativeButton : BiometryAuthError
    data object NoDeviceCredential : BiometryAuthError
    data object Unknown : BiometryAuthError

    companion object
}
