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
import proton.android.pass.common.api.some
import proton.android.pass.preferences.AppLockPreference
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.HasAuthenticated
import kotlin.time.Duration.Companion.minutes

class NeedsAuthCheckerTest {

    @Test
    fun `if biometric lock is disabled, no auth is needed`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Disabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockPreference = AppLockPreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now()
        )

        assertThat(res).isFalse()
    }

    @Test
    fun `if biometric lock is enabled and user has not authenticated, auth is required`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockPreference = AppLockPreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now()
        )

        assertThat(res).isTrue()
    }

    @Test
    fun `if AppLock is set to Immediately but is already authenticated, auth is not required`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockPreference = AppLockPreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now()
        )

        assertThat(res).isFalse()
    }

    @Test
    fun `if AppLock is set to a time, and it has not been that long, auth is not needed`() {

        val now = Clock.System.now()
        val oneMinuteAgo = now.minus(1.minutes)

        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockPreference = AppLockPreference.InTwoMinutes,
            lastUnlockTime = oneMinuteAgo.some(),
            now = now
        )

        assertThat(res).isFalse()
    }

    @Test
    fun `if lock time already elapsed and has not authenticated, auth is required`() {

        val now = Clock.System.now()
        val threeMinutesAgo = now.minus(3.minutes)

        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockPreference = AppLockPreference.InTwoMinutes,
            lastUnlockTime = threeMinutesAgo.some(),
            now = now
        )

        assertThat(res).isTrue()
    }

}
