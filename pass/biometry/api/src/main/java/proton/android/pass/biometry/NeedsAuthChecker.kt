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

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.HasAuthenticated

sealed interface NeedsAuthResult {
    fun value(): Boolean
}

sealed interface NeedsAuthReason : NeedsAuthResult {
    override fun value() = true

    object BootCountChanged : NeedsAuthReason
    object LastUnlockTimeNotSet : NeedsAuthReason
    object LastUnlockTimeInTheFuture : NeedsAuthReason
    object LockTimeElapsed : NeedsAuthReason
    object LockImmediatelyAndHadNotAuthenticated : NeedsAuthReason
}

sealed interface NoNeedsAuthReason : NeedsAuthResult {
    override fun value() = false

    object AuthDisabled : NoNeedsAuthReason
    object BiometricDisabled : NoNeedsAuthReason
    object LockTimeNotElapsed : NoNeedsAuthReason
    object LockImmediatelyButHadAuthenticated : NoNeedsAuthReason
}

object NeedsAuthChecker {

    private const val TAG = "NeedsAuthChecker"

    @Suppress("ReturnCount", "LongParameterList")
    fun needsAuth(
        appLockState: AppLockState,
        hasAuthenticated: HasAuthenticated,
        appLockTimePreference: AppLockTimePreference,
        lastUnlockTime: Option<Long>,
        now: Long,
        lastBootCount: Option<Long>,
        bootCount: Long,
    ): NeedsAuthResult {
        // Check for biometric lock. If it's disabled, no auth is needed.
        if (appLockState is AppLockState.Disabled) {
            PassLogger.d(TAG, "AppLockState.Disabled, no need to auth")
            return NoNeedsAuthReason.AuthDisabled
        }

        // Check if boot count has changed. If it has, needs auth
        if (lastBootCount is Some && lastBootCount.value != bootCount) {
            PassLogger.d(TAG, "Boot count has changed, needs auth")
            return NeedsAuthReason.BootCountChanged
        }

        if (appLockTimePreference == AppLockTimePreference.Immediately) {
            val needsAuth = hasAuthenticated is HasAuthenticated.NotAuthenticated
            PassLogger.d(TAG, "AppLockTimePreference.Immediately. NeedsAuth=$needsAuth")
            return if (needsAuth) {
                NeedsAuthReason.LockImmediatelyAndHadNotAuthenticated
            } else {
                NoNeedsAuthReason.LockImmediatelyButHadAuthenticated
            }

        }

        PassLogger.d(TAG, "Checking unlock time")

        // User has set an expiration time preference. Check if we need to perform auth again.
        val unlockTime = lastUnlockTime.value() ?: return NeedsAuthReason.LastUnlockTimeNotSet
        val appLockDuration = appLockTimePreference.toDuration()
        val timeSinceLastAuth = now - unlockTime
        if (timeSinceLastAuth < 0) {
            PassLogger.w(
                TAG,
                "Time since last auth is negative, reboot has happened. " +
                    "UnlockTime: $unlockTime | now: $now | timeSinceLastAuth: $timeSinceLastAuth"
            )
            return NeedsAuthReason.LastUnlockTimeInTheFuture
        }

        val shouldPerform = appLockDuration.inWholeMilliseconds < timeSinceLastAuth
        PassLogger.d(
            TAG,
            "timeSinceLastAuth: $timeSinceLastAuth |" +
                " appLockDuration: $appLockDuration | shouldPerformAuth: $shouldPerform"
        )
        return if (shouldPerform) {
            NeedsAuthReason.LockTimeElapsed
        } else {
            NoNeedsAuthReason.LockTimeNotElapsed
        }
    }
}
