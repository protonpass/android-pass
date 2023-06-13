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

        when (appLockPreference) {
            AppLockPreference.Immediately -> {
                val needsAuth = hasAuthenticated is HasAuthenticated.NotAuthenticated
                PassLogger.d(TAG, "AppLockPreference.Immediately. NeedsAuth=$needsAuth")
                return needsAuth
            }

            else -> {}
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
