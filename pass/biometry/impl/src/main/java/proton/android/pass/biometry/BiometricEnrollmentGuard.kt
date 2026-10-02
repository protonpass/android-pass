/*
 * Copyright (c) 2026 Proton AG
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

import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.UserNotAuthenticatedException
import proton.android.pass.log.api.PassLogger
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.ProviderException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricEnrollmentGuard @Inject constructor(
    private val guardKey: EnrollmentGuardKey
) {

    fun hasEnrollmentChanged(): Boolean = runCatching {
        runOrRetryOnce { guardKey.initCipher() }
    }.fold(
        onSuccess = { false },
        onFailure = { e ->
            when {
                e is KeyPermanentlyInvalidatedException -> {
                    PassLogger.w(TAG, "Biometric enrollment changed: key permanently invalidated")
                    PassLogger.w(TAG, e)
                    guardKey.delete()
                    true
                }
                e is UserNotAuthenticatedException -> {
                    // Expected for user-authenticated keys: the key is valid but requires recent auth.
                    PassLogger.d(TAG, "UserNotAuthenticatedException: key is valid, enrollment not changed")
                    false
                }
                e is InvalidKeyException -> {
                    // Some OEM variants throw InvalidKeyException (the parent class) instead of the
                    // more specific KeyPermanentlyInvalidatedException. Treat as enrollment changed.
                    PassLogger.w(TAG, "InvalidKeyException during enrollment check, treating as changed")
                    PassLogger.w(TAG, e)
                    guardKey.delete()
                    true
                }
                e.isNoBiometricEnrolled() -> {
                    PassLogger.i(TAG, "No strong biometric enrolled, enrollment guard not armed")
                    false
                }
                else -> {
                    // Unknown error: we cannot verify enrollment state. Fail closed to protect the user.
                    PassLogger.w(TAG, "Unknown error verifying biometric enrollment: ${e::class.simpleName}")
                    PassLogger.w(TAG, e)
                    true
                }
            }
        }
    )

    fun reset(): EnrollmentGuardSetupResult {
        guardKey.delete()
        return runCatching {
            runOrRetryOnce { guardKey.initCipher() }
        }.fold(
            onSuccess = { EnrollmentGuardSetupResult.Armed },
            onFailure = { e ->
                when {
                    e is UserNotAuthenticatedException -> EnrollmentGuardSetupResult.Armed
                    e.isNoBiometricEnrolled() -> {
                        PassLogger.i(TAG, "No strong biometric enrolled, enrollment guard not armed")
                        EnrollmentGuardSetupResult.NoBiometricEnrolled
                    }
                    else -> {
                        PassLogger.w(TAG, "Failed to set up biometric enrollment guard: ${e::class.simpleName}")
                        PassLogger.w(TAG, e)
                        guardKey.delete()
                        EnrollmentGuardSetupResult.Failed
                    }
                }
            }
        )
    }

    private fun Throwable.isNoBiometricEnrolled(): Boolean {
        val cause = cause
        return this is InvalidAlgorithmParameterException &&
            cause is IllegalStateException &&
            cause.message.orEmpty().contains(NO_BIOMETRIC_ENROLLED_MARKER, ignoreCase = true)
    }

    private fun <T> runOrRetryOnce(block: () -> T): T = runCatching { block() }
        .getOrElse { error ->
            when {
                error is KeyPermanentlyInvalidatedException ||
                    error is InvalidKeyException ||
                    error is UserNotAuthenticatedException ||
                    error.isNoBiometricEnrolled() -> throw error
                error is ProviderException -> logAndRetry(error, block)
                error is GeneralSecurityException -> logAndRetry(error, block)
                else -> throw error
            }
        }

    private fun <T> logAndRetry(error: Throwable, block: () -> T): T {
        PassLogger.w(TAG, "${LogTag.ENROLLMENT_CHECK_RETRY}: retrying after transient keystore failure")
        PassLogger.w(TAG, error)
        return block()
    }

    companion object {
        private const val TAG = "BiometricEnrollmentGuard"
        private const val NO_BIOMETRIC_ENROLLED_MARKER = "must be enrolled"
    }

    private object LogTag {
        const val ENROLLMENT_CHECK_RETRY = "pass.biometric.enrollment.check.retry"
    }
}
