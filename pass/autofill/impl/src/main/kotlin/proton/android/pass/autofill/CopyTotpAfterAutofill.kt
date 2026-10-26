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

package proton.android.pass.autofill

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import proton.android.pass.autofill.service.R
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates the current TOTP code for a decrypted provisioning URI and copies it to the
 * clipboard after an autofill, when the user has enabled "Copy authentication codes".
 *
 * Runs on an application-lifetime scope on purpose: both autofill entry points
 * (the item picker and the inline suggestions NoUi activity) finish themselves a few
 * milliseconds after requesting the copy, which would cancel a [androidx.lifecycle.ViewModel]-scoped
 * coroutine before the code is generated and copied. The clipboard is not owned by those
 * activities, so the copy must not die with them.
 */
@Singleton
class CopyTotpAfterAutofill @Inject constructor(
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val clipboardManager: ClipboardManager,
    private val toastManager: ToastManager,
    private val telemetryManager: TelemetryManager,
    private val preferenceRepository: UserPreferencesRepository,
    private val appDispatchers: AppDispatchers
) {

    private val scope = CoroutineScope(
        SupervisorJob() +
            appDispatchers.main +
            CoroutineExceptionHandler { _, error ->
                PassLogger.w(TAG, "Could not copy totp code")
                PassLogger.w(TAG, error)
            }
    )

    fun run(decryptedTotpUri: String) {
        if (decryptedTotpUri.isBlank()) return

        scope.launch {
            if (!preferenceRepository.getCopyTotpToClipboardEnabled().first().value()) return@launch

            getTotpCodeFromUri(decryptedTotpUri)
                .onSuccess {
                    clipboardManager.copyToClipboard(it, isSecure = true)
                    telemetryManager.sendEvent(MFAAutofillCopied)
                    toastManager.showToast(R.string.autofill_notification_copy_to_clipboard)
                }
                .onFailure { error ->
                    PassLogger.w(TAG, "Could not copy totp code")
                    PassLogger.w(TAG, error)
                }
        }
    }

    internal companion object {
        private const val TAG = "CopyTotpAfterAutofill"
    }
}
