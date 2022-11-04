package me.proton.android.pass.biometry

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
import me.proton.android.pass.biometry.extensions.from
import me.proton.android.pass.biometry.implementation.R
import me.proton.android.pass.log.PassLogger
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Some
import javax.inject.Inject

class BiometryManagerImpl @Inject constructor(
    private val biometricManager: BiometricManager
) : BiometryManager {

    override fun getBiometryStatus(): BiometryStatus =
        when (val res = canAuthenticate()) {
            BiometryResult.Success -> BiometryStatus.CanAuthenticate
            is BiometryResult.FailedToStart -> when (res.cause) {
                BiometryStartupError.NoneEnrolled -> BiometryStatus.NotEnrolled
                else -> BiometryStatus.NotAvailable
            }
            else -> BiometryStatus.NotAvailable
        }

    override fun launch(context: ContextHolder): Flow<BiometryResult> = channelFlow {
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
                trySend(BiometryResult.Success)
                close()
            }
        }

        val ctx = when (val ctx = context.getContext()) {
            None -> {
                PassLogger.i(TAG, "Received None context")
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
                PassLogger.i(TAG, "Context is not FragmentActivity")
                trySend(BiometryResult.FailedToStart(BiometryStartupError.Unknown))
                close()
                return@channelFlow
            }
        }

        prompt.authenticate(getPromptInfo(ctx))
        awaitClose()
    }

    private fun canAuthenticate(): BiometryResult {
        val res = biometricManager.canAuthenticate(getAllowedAuthenticators())
        return BiometryResult.from(res)
    }

    private fun getPromptInfo(context: Context): PromptInfo =
        PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setAllowedAuthenticators(getAllowedAuthenticators())
            .build()


    // https://developer.android.com/reference/kotlin/androidx/biometric/BiometricPrompt.PromptInfo.Builder#setallowedauthenticators
    // BIOMETRIC_STRONG | DEVICE_CREDENTIAL is unsupported on API 28-29.
    // Setting an unsupported value on an affected Android version will result in an error
    // when calling build().

    private fun getAllowedAuthenticators(): Int =
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ||
            Build.VERSION.SDK_INT == Build.VERSION_CODES.P
        ) {
            AUTHENTICATORS_P_OR_Q
        } else {
            AUTHENTICATORS_NOT_P_NOT_Q
        }


    companion object {
        private const val TAG = "BiometryLauncherImpl"

        private const val AUTHENTICATORS_NOT_P_NOT_Q =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        private const val AUTHENTICATORS_P_OR_Q =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    }

}
