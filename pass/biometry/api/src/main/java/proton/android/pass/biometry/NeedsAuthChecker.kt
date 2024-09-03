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
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.HasAuthenticated
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

sealed interface NeedsAuthResult {
    fun value(): Boolean
}

sealed interface NeedsAuthReason : NeedsAuthResult {
    override fun value() = true

    data object BootCountChanged : NeedsAuthReason
    data object LastUnlockTimeNotSet : NeedsAuthReason
    data object LastUnlockTimeInTheFuture : NeedsAuthReason
    data object LockTimeElapsed : NeedsAuthReason
    data object LockImmediatelyAndHadNotAuthenticated : NeedsAuthReason
}

sealed interface NoNeedsAuthReason : NeedsAuthResult {
    override fun value() = false

    data object AuthDisabled : NoNeedsAuthReason
    data object LockTimeNotElapsed : NoNeedsAuthReason
    data object LockImmediatelyButHadAuthenticated : NoNeedsAuthReason
}

object NeedsAuthChecker {

    private const val TAG = "NeedsAuthChecker"

    @Suppress("LongParameterList")
    fun needsAuth(
        appLockState: AppLockState,
        hasAuthenticated: HasAuthenticated,
        appLockTimePreference: AppLockTimePreference,
        lastUnlockTime: Option<Long>,
        now: Long,
        lastBootCount: Option<Long>,
        bootCount: Long,
        organizationSettings: Option<OrganizationSettings>
    ): NeedsAuthResult {
        if (
            organizationSettings is Some &&
            organizationSettings.value.isEnforced() &&
            hasAuthenticated is HasAuthenticated.NotAuthenticated
        ) {
            return checkUnlockTime(
                lastUnlockTime = lastUnlockTime,
                now = now,
                appLockDuration = organizationSettings.value.secondsToForceLock()
                    .toDuration(DurationUnit.SECONDS)
            )
        }

        if (appLockState is AppLockState.Disabled) {
            PassLogger.d(TAG, "AppLockState.Disabled, no need to auth")
            return NoNeedsAuthReason.AuthDisabled
        }

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

        return checkUnlockTime(
            lastUnlockTime = lastUnlockTime,
            now = now,
            appLockDuration = appLockTimePreference.toDuration()
        )
    }

    private fun checkUnlockTime(
        lastUnlockTime: Option<Long>,
        now: Long,
        appLockDuration: Duration
    ): NeedsAuthResult {
        PassLogger.d(TAG, "Checking unlock time")

        val unlockTime = lastUnlockTime.value() ?: return NeedsAuthReason.LastUnlockTimeNotSet
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
        return if (shouldPerform) NeedsAuthReason.LockTimeElapsed else NoNeedsAuthReason.LockTimeNotElapsed
    }

}
