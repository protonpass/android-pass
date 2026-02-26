/*
 * Copyright (c) 2024-2026 Proton AG
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
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.isQuest
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.CanDeviceSupportPasskeys
import proton.android.pass.passkeys.api.PasskeySupport
import javax.inject.Inject

class CanDeviceSupportPasskeysImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appConfig: AppConfig
) : CanDeviceSupportPasskeys {

    @Suppress("NewApi")
    override fun invoke(): PasskeySupport = when {
        appConfig.flavor.isQuest() -> {
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
            logCredentialManagerDiagnostics()
            PassLogger.i(TAG, "Device is capable of passkeys")
            PasskeySupport.CanSupport
        }
    }

    private fun logCredentialManagerDiagnostics() {
        runCatching {
            androidx.credentials.CredentialManager.create(context)
            PassLogger.i(TAG, "CredentialManager instance created")
        }.onFailure { t ->
            PassLogger.i(TAG, "CredentialManager create() threw ${t::class.simpleName}")
        }
    }

    companion object {
        private const val TAG = "CanDeviceSupportPasskeysImpl"
    }
}
