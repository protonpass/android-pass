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

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.security.keystore.UserNotAuthenticatedException
import proton.android.pass.log.api.PassLogger
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricEnrollmentGuard @Inject constructor() {

    @Suppress("TooGenericExceptionCaught")
    fun hasEnrollmentChanged(): Boolean = try {
        runOrRetryOnce {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            // Keystore is known to be sensitive to parallel operations.
            synchronized(lock) {
                cipher.init(Cipher.ENCRYPT_MODE, key)
            }
        }
        false
    } catch (e: KeyPermanentlyInvalidatedException) {
        PassLogger.w(TAG, "Biometric enrollment changed: key permanently invalidated")
        PassLogger.w(TAG, e)
        deleteGuardKey()
        true
    } catch (e: InvalidKeyException) {
        // Some OEM variants throw InvalidKeyException (the parent class) instead of the
        // more specific KeyPermanentlyInvalidatedException. Treat as enrollment changed.
        PassLogger.w(TAG, "InvalidKeyException during enrollment check, treating as changed")
        PassLogger.w(TAG, e)
        deleteGuardKey()
        true
    } catch (_: UserNotAuthenticatedException) {
        // Expected for user-authenticated keys: the key is valid but requires recent auth.
        PassLogger.d(TAG, "UserNotAuthenticatedException: key is valid, enrollment not changed")
        false
    } catch (e: Exception) {
        // Unknown error: we cannot verify enrollment state. Fail closed to protect the user.
        PassLogger.w(TAG, "Unknown error verifying biometric enrollment: ${e::class.simpleName}")
        PassLogger.w(TAG, e)
        true
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) return existingKey
        if (existingKey != null) {
            PassLogger.w(TAG, "Unexpected non-secret key for guard alias, recreating")
            deleteGuardKey()
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    private fun deleteGuardKey() {
        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        }.onFailure { deleteError ->
            PassLogger.w(TAG, "Failed to delete biometric enrollment guard key")
            PassLogger.w(TAG, deleteError)
        }
    }

    private fun <T> runOrRetryOnce(block: () -> T): T = try {
        block()
    } catch (error: KeyPermanentlyInvalidatedException) {
        throw error
    } catch (error: InvalidKeyException) {
        throw error
    } catch (error: UserNotAuthenticatedException) {
        throw error
    } catch (error: ProviderException) {
        logAndRetry(error, block)
    } catch (error: GeneralSecurityException) {
        logAndRetry(error, block)
    }

    private fun <T> logAndRetry(error: Throwable, block: () -> T): T {
        PassLogger.w(TAG, "${LogTag.ENROLLMENT_CHECK_RETRY}: retrying after transient keystore failure")
        PassLogger.w(TAG, error)
        return block()
    }

    companion object {
        private const val TAG = "BiometricEnrollmentGuard"
        private const val KEY_ALIAS = "proton_pass_biometric_enrollment_guard"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private val lock = Any()
    }

    private object LogTag {
        const val ENROLLMENT_CHECK_RETRY = "pass.biometric.enrollment.check.retry"
    }
}
