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

package proton.android.pass.autofill.ui.autofill

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.TestUpdateAutofillItem
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.inappreview.fakes.TestInAppReviewTriggerMetrics
import proton.android.pass.notifications.fakes.TestToastManager
import proton.android.pass.preferences.TestInternalSettingsRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.totp.fakes.TestGetTotpCodeFromUri

class AutofillAppViewModelTest {

    private lateinit var instance: AutofillAppViewModel

    private lateinit var updateAutofillItem: TestUpdateAutofillItem

    @Before
    fun setup() {
        updateAutofillItem = TestUpdateAutofillItem()

        instance = AutofillAppViewModel(
            encryptionContextProvider = TestEncryptionContextProvider(),
            clipboardManager = TestClipboardManager(),
            getTotpCodeFromUri = TestGetTotpCodeFromUri(),
            toastManager = TestToastManager(),
            updateAutofillItem = updateAutofillItem,
            preferenceRepository = TestPreferenceRepository(),
            telemetryManager = TestTelemetryManager(),
            inAppReviewTriggerMetrics = TestInAppReviewTriggerMetrics(),
            getItemById = FakeGetItemById(),
            internalSettingsRepository = TestInternalSettingsRepository(),
            clock = Clock.System
        )
    }

    @Test
    fun `does not send packageName to updateAutofillItem if is browser`() = runTest {
        val (item, state) = getInitialData("com.android.chrome")
        instance.sendMappings(item, state, false)

        val memory = updateAutofillItem.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val memoryItem = memory.first()
        assertThat(memoryItem.packageInfo).isEqualTo(None)
    }

    @Test
    fun `sends packageName to updateAutofillItem if is not browser`() = runTest {
        val packageName = "some.other.app"
        val (item, state) = getInitialData(packageName)
        instance.sendMappings(item, state, false)

        val memory = updateAutofillItem.getMemory()
        assertThat(memory.size).isEqualTo(1)

        val packageInfo = memory.first().packageInfo.value()
        assertThat(packageInfo).isNotNull()

        assertThat(packageInfo!!.packageName.value).isEqualTo(packageName)
    }

    private fun getInitialData(packageName: String): Pair<AutofillItem, AutofillAppState> = AutofillItem.Login(
        itemId = "test-item-id",
        shareId = "share-id",
        username = "username",
        password = null,
        totp = null,
        shouldLinkPackageName = false,
        userId = "userID"
    ) to AutofillAppState(
        autofillData = AutofillData(
            assistInfo = AssistInfo(cluster = NodeCluster.Empty, url = None),
            packageInfo = PackageInfo(
                packageName = PackageName(packageName),
                appName = AppName("Test app name")
            ),
            isDangerousAutofill = false
        ),
        packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
    )

}
