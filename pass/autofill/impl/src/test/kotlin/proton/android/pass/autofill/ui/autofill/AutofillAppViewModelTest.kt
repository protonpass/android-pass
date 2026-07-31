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

package proton.android.pass.autofill.ui.autofill

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeUpdateAutofillItem
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.inappreview.fakes.FakeInAppReviewTriggerMetrics
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.totp.fakes.FakeGetTotpCodeFromUri

class AutofillAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var instance: AutofillAppViewModel

    private lateinit var updateAutofillItem: FakeUpdateAutofillItem

    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository

    private lateinit var getItemById: FakeGetItemById

    @Before
    fun setup() {
        updateAutofillItem = FakeUpdateAutofillItem()
        internalSettingsRepository = FakeInternalSettingsRepository()
        getItemById = FakeGetItemById()

        instance = AutofillAppViewModel(
            encryptionContextProvider = FakeEncryptionContextProvider(),
            clipboardManager = FakeClipboardManager(),
            getTotpCodeFromUri = FakeGetTotpCodeFromUri(),
            toastManager = FakeToastManager(),
            updateAutofillItem = updateAutofillItem,
            preferenceRepository = FakePreferenceRepository(),
            telemetryManager = FakeTelemetryManager(),
            inAppReviewTriggerMetrics = FakeInAppReviewTriggerMetrics(),
            getItemById = getItemById,
            internalSettingsRepository = internalSettingsRepository,
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

    @Test
    fun `does not show warning dialog for a trusted package`() = runTest {
        val packageName = "com.android.chrome"
        val fingerprints = setOf("AA")
        val (item, state) = getInitialData(packageName, isDangerousAutofill = true, hashes = fingerprints)
        getItemById.emit(
            shareId = item.shareId(),
            itemId = item.itemId(),
            value = Result.success(buildTestItem(shareId = item.shareId(), itemId = item.itemId()))
        )
        internalSettingsRepository.addTrustedAutofillPackage(packageName, fingerprints)

        instance.onItemSelected(state = state, autofillItem = item, isSuggestion = false)

        assertThat(instance.stateFlow.value).isNotInstanceOf(AutofillAppEvent.ShowWarningDialog::class.java)
    }

    @Test
    fun `shows warning dialog when trusted package fingerprint does not match current fingerprint`() = runTest {
        val packageName = "com.android.chrome"
        val (item, state) = getInitialData(packageName, isDangerousAutofill = true, hashes = setOf("BB"))
        getItemById.emit(
            shareId = item.shareId(),
            itemId = item.itemId(),
            value = Result.success(buildTestItem(shareId = item.shareId(), itemId = item.itemId()))
        )
        internalSettingsRepository.addTrustedAutofillPackage(packageName, setOf("AA"))

        instance.onItemSelected(state = state, autofillItem = item, isSuggestion = false)

        assertThat(instance.stateFlow.value).isInstanceOf(AutofillAppEvent.ShowWarningDialog::class.java)
    }

    private fun buildTestItem(shareId: ShareId, itemId: ItemId): Item = Item(
        id = itemId,
        userId = UserId("userID"),
        itemUuid = "item-uuid",
        revision = 1,
        shareId = shareId,
        folderId = null,
        itemType = ItemType.Login(
            itemEmail = "",
            itemUsername = "username",
            password = "",
            websites = emptyList(),
            packageInfoSet = emptySet(),
            primaryTotp = "",
            customFields = emptyList(),
            passkeys = emptyList(),
            autofillUrls = emptyList()
        ),
        title = FakeEncryptionContext.encrypt("Test title"),
        note = FakeEncryptionContext.encrypt(""),
        content = EncryptedByteArray(byteArrayOf()),
        state = 1,
        packageInfoSet = emptySet(),
        createTime = Instant.fromEpochSeconds(0),
        modificationTime = Instant.fromEpochSeconds(0),
        lastAutofillTime = None,
        isPinned = false,
        pinTime = None,
        itemFlags = ItemFlags(0),
        shareCount = 0,
        contentFormatVersion = 1,
        shareType = ShareType.Vault
    )

    private fun getInitialData(
        packageName: String,
        isDangerousAutofill: Boolean = false,
        hashes: Set<String> = emptySet()
    ): Pair<AutofillItem, AutofillAppState> = AutofillItem.Login(
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
                appName = AppName("Test app name"),
                hashes = hashes
            ),
            isDangerousAutofill = isDangerousAutofill
        ),
        packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
    )

}
