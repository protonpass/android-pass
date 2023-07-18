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

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import proton.android.pass.biometry.extensions.from
import proton.android.pass.biometry.implementation.R
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.BiometricSystemLockPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometryManagerImpl @Inject constructor(
    private val biometricManager: BiometricManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val storeAuthSuccessful: StoreAuthSuccessful
) : BiometryManager {

    override fun getBiometryStatus(): BiometryStatus =
        when (val res = canAuthenticate()) {
            BiometryResult.Success -> {
                PassLogger.i(TAG, "Biometry")
                BiometryStatus.CanAuthenticate
            }

            is BiometryResult.FailedToStart -> when (res.cause) {
                BiometryStartupError.NoneEnrolled -> BiometryStatus.NotEnrolled
                else -> BiometryStatus.NotAvailable
            }

            else -> BiometryStatus.NotAvailable
        }

    override fun launch(contextHolder: ClassHolder<Context>): Flow<BiometryResult> = channelFlow {
        val canAuthenticate = canAuthenticate()
        if (canAuthenticate is BiometryResult.FailedToStart) {
            trySend(canAuthenticate)
            close()
            return@channelFlow
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                PassLogger.i(TAG, "Auth error [code=$errorCode]: $errString")
                trySend(BiometryResult.Error(BiometryAuthError.from(errorCode)))
                close()
            }

            override fun onAuthenticationFailed() {
                PassLogger.i(TAG, "Auth failed")
                trySend(BiometryResult.Failed)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                PassLogger.i(TAG, "Auth succeeded")
                storeAuthSuccessful()
                trySend(BiometryResult.Success)
                close()
            }
        }

        val ctx = when (val ctx = contextHolder.get()) {
            None -> {
                val message = "Received None context"
                PassLogger.e(TAG, IllegalArgumentException(message), message)
                trySend(BiometryResult.FailedToStart(BiometryStartupError.Unknown))
                return@channelFlow
            }

            is Some -> ctx.value
        }

        val prompt = when (ctx) {
            is FragmentActivity -> BiometricPrompt(
                ctx,
                ContextCompat.getMainExecutor(ctx),
                callback
            )

            else -> {
                val message = "Context is not FragmentActivity"
                PassLogger.e(TAG, IllegalArgumentException(message), message)
                trySend(BiometryResult.FailedToStart(BiometryStartupError.Unknown))
                close()
                return@channelFlow
            }
        }

        PassLogger.i(TAG, "Starting biometry authentication")
        val biometricSystemLock = userPreferencesRepository.getBiometricSystemLockPreference()
            .first()
        prompt.authenticate(getPromptInfo(ctx, biometricSystemLock))
        awaitClose()
    }

    private fun canAuthenticate(): BiometryResult {
        val biometricSystemLock = runBlocking {
            userPreferencesRepository.getBiometricSystemLockPreference().first()
        }
        val res = biometricManager.canAuthenticate(getAllowedAuthenticators(biometricSystemLock))
        return BiometryResult.from(res)
    }

    private fun getPromptInfo(
        context: Context,
        biometricSystemLock: BiometricSystemLockPreference
    ): PromptInfo {
        val builder = PromptInfo.Builder()
        builder.setTitle(context.getString(R.string.biometric_prompt_title))
        builder.setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
        when (biometricSystemLock) {
            BiometricSystemLockPreference.Enabled -> {
                builder.setAllowedAuthenticators(getAllowedAuthenticators(biometricSystemLock))
            }

            BiometricSystemLockPreference.NotEnabled -> {
                builder.setAllowedAuthenticators(getAllowedAuthenticators(biometricSystemLock))
                builder.setNegativeButtonText(context.getString(R.string.biometric_prompt_cancel))
            }
        }
        return builder.build()
    }


    // https://developer.android.com/reference/kotlin/androidx/biometric/BiometricPrompt.PromptInfo.Builder#setallowedauthenticators
    // BIOMETRIC_STRONG | DEVICE_CREDENTIAL is unsupported on API 28-29.
    // Setting an unsupported value on an affected Android version will result in an error
    // when calling build().

    private fun getAllowedAuthenticators(biometricSystemLock: BiometricSystemLockPreference): Int =
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ||
            Build.VERSION.SDK_INT == Build.VERSION_CODES.P
        ) {
            PassLogger.i(TAG, "Allowed authenticators: AUTHENTICATORS_P_OR_Q")
            BiometricManager.Authenticators.BIOMETRIC_WEAK.orDevice(biometricSystemLock)
        } else {
            PassLogger.i(TAG, "Allowed authenticators: AUTHENTICATORS_NOT_P_NOT_Q")
            BiometricManager.Authenticators.BIOMETRIC_STRONG.orDevice(biometricSystemLock)
        }

    private fun Int.orDevice(biometricSystemLock: BiometricSystemLockPreference): Int =
        if (biometricSystemLock.value()) {
            this or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            this
        }

    companion object {
        private const val TAG = "BiometryLauncherImpl"
    }

}
