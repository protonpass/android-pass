/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.passkeys.impl

import android.content.Context
import android.credentials.CredentialManager
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.CheckPasskeySupport
import proton.android.pass.passkeys.api.PasskeySupport
import javax.inject.Inject

class CheckPasskeySupportImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appConfig: AppConfig
) : CheckPasskeySupport {

    @Suppress("NewApi")
    override fun invoke(): PasskeySupport = runCatching {
        when {
            appConfig.flavor is BuildFlavor.Quest -> {
                PassLogger.i(
                    TAG,
                    "Passkey support not available on Quest"
                )
                PasskeySupport.NotSupported(PasskeySupport.NotSupportedReason.Quest)
            }

            appConfig.androidVersion < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                PassLogger.i(
                    TAG,
                    "Passkey support not available on Android version ${appConfig.androidVersion}"
                )
                PasskeySupport.NotSupported(PasskeySupport.NotSupportedReason.AndroidVersion)
            }

            else -> {
                checkCredentialManager()
            }
        }
    }.getOrElse {
        PassLogger.w(TAG, "Error checking passkey support")
        PassLogger.w(TAG, it)
        PasskeySupport.NotSupported(PasskeySupport.NotSupportedReason.Unknown)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun checkCredentialManager(): PasskeySupport {
        val credentialManager = appContext.getSystemService(CredentialManager::class.java)
        return if (credentialManager == null) {
            PassLogger.i(TAG, "CredentialManager not supported")
            PasskeySupport.NotSupported(PasskeySupport.NotSupportedReason.CredentialManagerUnsupported)
        } else {
            PassLogger.i(TAG, "Passkeys are supported")
            PasskeySupport.Supported
        }
    }

    companion object {
        private const val TAG = "CheckPasskeySupportImpl"
    }
}
