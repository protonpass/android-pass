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

package proton.android.pass.autofill.ui.autofill

import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import org.junit.Test
import proton.android.pass.autofill.CopyTotpAfterAutofill
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeHasActiveAccount
import proton.android.pass.data.fakes.usecases.FakeUpdateAutofillItem
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.inappreview.fakes.FakeInAppReviewTriggerMetrics
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.totp.fakes.FakeGetTotpCodeFromUri

class AutofillAppViewModelTotpCopyTest {

    @Test
    fun `copies the totp code even when the viewmodel is cleared before the code is generated`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(standardDispatcher)
        try {
            val clipboardManager = FakeClipboardManager()
            val getTotpCodeFromUri = FakeGetTotpCodeFromUri().apply {
                setResult(Result.success(CODE))
            }
            val preferenceRepository = FakePreferenceRepository().apply {
                setCopyTotpToClipboardEnabled(CopyTotpToClipboard.Enabled)
            }
            val instance = AutofillAppViewModel(
                encryptionContextProvider = FakeEncryptionContextProvider(),
                copyTotpAfterAutofill = CopyTotpAfterAutofill(
                    getTotpCodeFromUri = getTotpCodeFromUri,
                    clipboardManager = clipboardManager,
                    toastManager = FakeToastManager(),
                    telemetryManager = FakeTelemetryManager(),
                    preferenceRepository = preferenceRepository,
                    appDispatchers = FakeAppDispatchers.withTestDispatcher(standardDispatcher)
                ),
                updateAutofillItem = FakeUpdateAutofillItem(),
                telemetryManager = FakeTelemetryManager(),
                inAppReviewTriggerMetrics = FakeInAppReviewTriggerMetrics(),
                getItemById = FakeGetItemById(),
                hasActiveAccount = FakeHasActiveAccount(),
                internalSettingsRepository = FakeInternalSettingsRepository(),
                clock = Clock.System
            )

            var viewModelWorkRan = false
            instance.viewModelScope.launch { viewModelWorkRan = true }

            instance.sendMappings(buildLoginWithTotp(), buildState(), false)
            instance.viewModelScope.cancel()

            advanceUntilIdle()

            assertThat(viewModelWorkRan).isFalse()
            assertThat(clipboardManager.getContents()).isEqualTo(CODE)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun buildLoginWithTotp(): AutofillItem.Login = AutofillItem.Login(
        userId = "userID",
        itemId = "test-item-id",
        shareId = "share-id",
        username = "username",
        password = null,
        totp = FakeEncryptionContext.encrypt(TOTP_URI),
        shouldLinkPackageName = false
    )

    private fun buildState(): AutofillAppState = AutofillAppState(
        autofillData = AutofillData(
            assistInfo = AssistInfo(cluster = NodeCluster.Empty, url = None),
            packageInfo = PackageInfo(
                packageName = PackageName("com.android.chrome"),
                appName = AppName("Test app name"),
                hashes = emptySet()
            ),
            isDangerousAutofill = false
        ),
        packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
    )

    private companion object {
        const val CODE = "123456"
        const val TOTP_URI = "otpauth://totp/example.com:user?secret=JBSWY3DPEHPK3PXP&issuer=example"
    }
}
