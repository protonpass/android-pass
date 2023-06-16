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

package proton.android.pass.biometry.extensions

import androidx.biometric.BiometricPrompt
import proton.android.pass.biometry.BiometryAuthError

@Suppress("ComplexMethod")
fun BiometryAuthError.Companion.from(value: Int): BiometryAuthError =
    when (value) {
        BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometryAuthError.HardwareUnavailable
        BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> BiometryAuthError.UnableToProcess
        BiometricPrompt.ERROR_TIMEOUT -> BiometryAuthError.Timeout
        BiometricPrompt.ERROR_NO_SPACE -> BiometryAuthError.NoSpace
        BiometricPrompt.ERROR_CANCELED -> BiometryAuthError.Canceled
        BiometricPrompt.ERROR_LOCKOUT -> BiometryAuthError.Lockout
        BiometricPrompt.ERROR_VENDOR -> BiometryAuthError.Vendor
        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometryAuthError.LockoutPermanent
        BiometricPrompt.ERROR_USER_CANCELED -> BiometryAuthError.UserCanceled
        BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometryAuthError.NoBiometrics
        BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometryAuthError.HardwareNotPresent
        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometryAuthError.NegativeButton
        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> BiometryAuthError.NoDeviceCredential
        else -> BiometryAuthError.Unknown
    }


