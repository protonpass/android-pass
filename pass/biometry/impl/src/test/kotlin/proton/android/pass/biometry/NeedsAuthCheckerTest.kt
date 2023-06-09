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
    fun `if AppLock is set to Immediately, auth is required`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockPreference = AppLockPreference.Immediately,
            lastUnlockTime = None,
            now = Clock.System.now()
        )

        assertThat(res).isTrue()
    }

    @Test
    fun `if AppLock is set to Never, auth is required if the user has not authenticated`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.NotAuthenticated,
            appLockPreference = AppLockPreference.Never,
            lastUnlockTime = None,
            now = Clock.System.now()
        )

        assertThat(res).isTrue()
    }

    @Test
    fun `if AppLock is set to Never, auth is not required if the user has already authenticated`() {
        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockPreference = AppLockPreference.Never,
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
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockPreference = AppLockPreference.InTwoMinutes,
            lastUnlockTime = oneMinuteAgo.some(),
            now = now
        )

        assertThat(res).isFalse()
    }

    @Test
    fun `if AppLock is set to a time, and it has has been more than that, auth is required`() {

        val now = Clock.System.now()
        val threeMinutesAgo = now.minus(3.minutes)

        val res = NeedsAuthChecker.needsAuth(
            biometricLock = BiometricLockState.Enabled,
            hasAuthenticated = HasAuthenticated.Authenticated,
            appLockPreference = AppLockPreference.InTwoMinutes,
            lastUnlockTime = threeMinutesAgo.some(),
            now = now
        )

        assertThat(res).isTrue()
    }

}
