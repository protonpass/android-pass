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

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Clock
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.HasAuthenticated
import kotlin.time.Duration.Companion.minutes

class NeedsAuthCheckerTest {

    @Test
    fun `if biometric lock is disabled, no auth is needed`() {
        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Disabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now().toEpochMilliseconds(),
            lastBootCount = Some(0),
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NoNeedsAuthReason.BiometricDisabled::class.java)
    }

    @Test
    fun `if biometric lock is enabled and user has not authenticated, auth is required`() {
        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now().toEpochMilliseconds(),
            lastBootCount = Some(0),
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NeedsAuthReason.LockImmediatelyAndHadNotAuthenticated::class.java)
    }

    @Test
    fun `if AppLock is set to Immediately but is already authenticated, auth is not required`() {
        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockTimePreference = AppLockTimePreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now().toEpochMilliseconds(),
            lastBootCount = Some(0),
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NoNeedsAuthReason.LockImmediatelyButHadAuthenticated::class.java)
    }

    @Test
    fun `if AppLock is set to a time, and it has not been that long, auth is not needed`() {
        val now = Clock.System.now()
        val oneMinuteAgo = now.minus(1.minutes)

        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.InTwoMinutes,
            lastUnlockTime = oneMinuteAgo.toEpochMilliseconds().some(),
            now = now.toEpochMilliseconds(),
            lastBootCount = Some(0),
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NoNeedsAuthReason.LockTimeNotElapsed::class.java)
    }

    @Test
    fun `if lock time already elapsed and has not authenticated, auth is required`() {

        val now = Clock.System.now()
        val threeMinutesAgo = now.minus(3.minutes)

        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.InTwoMinutes,
            lastUnlockTime = threeMinutesAgo.toEpochMilliseconds().some(),
            now = now.toEpochMilliseconds(),
            lastBootCount = None,
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NeedsAuthReason.LockTimeElapsed::class.java)
    }

    @Test
    fun `if lock time is in the past, auth is required`() {
        val now = Clock.System.now()
        val inThreeMinutes = now.plus(3.minutes)

        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.InTwoMinutes,
            lastUnlockTime = inThreeMinutes.toEpochMilliseconds().some(),
            now = now.toEpochMilliseconds(),
            lastBootCount = Some(0),
            bootCount = 0,
            appLockTypeState = appLockTypeState
        )

        assertThat(res).isInstanceOf(NeedsAuthReason.LastUnlockTimeInTheFuture::class.java)
    }


    @Test
    fun `if boot count has changed, auth is required`() {
        val now = Clock.System.now()
        val oneMinuteAgo = now.minus(1.minutes)

        val res = NeedsAuthChecker.needsAuth(
            appLockState = AppLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockTimePreference = AppLockTimePreference.InTwoMinutes,
            lastUnlockTime = oneMinuteAgo.toEpochMilliseconds().some(),
            now = now.toEpochMilliseconds(),
            lastBootCount = Some(1),
            bootCount = 2,
            appLockTypeState = appLockTypeState
        )


        assertThat(res).isInstanceOf(NeedsAuthReason.BootCountChanged::class.java)
    }

}
