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

import kotlinx.datetime.Instant
import proton.android.pass.common.api.Option
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockPreference
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.HasAuthenticated

object NeedsAuthChecker {

    private const val TAG = "NeedsAuthChecker"

    @Suppress("ReturnCount")
    fun needsAuth(
        biometricLock: BiometricLockState,
        hasAuthenticated: HasAuthenticated,
        appLockPreference: AppLockPreference,
        lastUnlockTime: Option<Instant>,
        now: Instant
    ): Boolean {
        // Check for biometric lock. If it's disabled, no auth is needed.
        if (biometricLock is BiometricLockState.Disabled) {
            PassLogger.d(TAG, "BiometricStateLock.Disabled, no need to auth")
            return false
        }

        if (appLockPreference == AppLockPreference.Immediately) {
            val needsAuth = hasAuthenticated is HasAuthenticated.NotAuthenticated
            PassLogger.d(TAG, "AppLockPreference.Immediately. NeedsAuth=$needsAuth")
            return needsAuth
        }

        PassLogger.d(TAG, "Checking unlock time")

        // User has set an expiration time preference. Check if we need to perform auth again.
        val unlockTime = lastUnlockTime.value() ?: return true
        val appLockDuration = appLockPreference.toDuration()
        val timeSinceLastAuth = now - unlockTime
        val shouldPerform = appLockDuration < timeSinceLastAuth
        PassLogger.d(
            TAG,
            "timeSinceLastAuth: $timeSinceLastAuth |" +
                " appLockDuration: $appLockDuration | shouldPerformAuth: $shouldPerform"
        )
        return shouldPerform
    }
}
