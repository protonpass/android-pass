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

import androidx.biometric.BiometricManager
import proton.android.pass.biometry.BiometryResult

@Suppress("ComplexMethod")
fun BiometryResult.Companion.from(value: Int): BiometryResult =
    when (value) {
        BiometricManager.BIOMETRIC_SUCCESS -> BiometryResult.Success
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unknown)
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unsupported)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.HardwareUnavailable)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.NoneEnrolled)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.NoHardware)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
            BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.SecurityUpdateRequired)
        else -> BiometryResult.FailedToStart(proton.android.pass.biometry.BiometryStartupError.Unknown)
    }
