/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.login

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.fakes.TestEmailValidator
import proton.android.pass.commonrust.fakes.passwords.strengths.TestPasswordStrengthCalculator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.attachments.FakeRenameAttachments
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.data.fakes.work.FakeWorkerLauncher
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.api.TotpSpec
import proton.android.pass.totp.fakes.TestTotpManager

class UpdateLoginViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: UpdateLoginViewModel

    private lateinit var getItemById: TestObserveItemById
    private lateinit var totpManager: TestTotpManager
    private lateinit var updateItem: TestUpdateItem
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        getItemById = TestObserveItemById()
        totpManager = TestTotpManager()
        updateItem = TestUpdateItem()
        snackbarDispatcher = TestSnackbarDispatcher()

        instance = UpdateLoginViewModel(
            getItemById = getItemById,
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("UserId"))
            },
            clipboardManager = TestClipboardManager(),
            totpManager = totpManager,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = TestSavedStateHandleProvider().apply {
                get()[CommonOptionalNavArgId.ShareId.key] = SHARE_ID
                get()[CommonNavArgId.ItemId.key] = ITEM_ID
            },
            encryptionContextProvider = TestEncryptionContextProvider(),
            passwordStrengthCalculator = TestPasswordStrengthCalculator(),
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) },
            telemetryManager = TestTelemetryManager(),
            draftRepository = TestDraftRepository(),
            observeUpgradeInfo = TestObserveUpgradeInfo(),
            updateItem = updateItem,
            createAlias = TestCreateAlias(),
            featureFlagsRepository = TestFeatureFlagsPreferenceRepository(),
            emailValidator = TestEmailValidator(),
            observeTooltipEnabled = FakeObserveTooltipEnabled(),
            disableTooltip = FakeDisableTooltip(),
            userPreferencesRepository = TestPreferenceRepository(),
            workerLauncher = FakeWorkerLauncher(),
            attachmentsHandler = proton.android.pass.features.itemcreate.attachments.FakeAttachmentHandler(),
            linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
            renameAttachments = FakeRenameAttachments(),
            customFieldDraftRepository = CustomFieldDraftRepositoryImpl()
        )
    }

    @Test
    fun `item with totp using default parameters shows only secret`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA1&period=30&digits=6"
        val primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(uri), uri)
        val item = TestObserveItems.createItem(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                password = HiddenState.Empty(TestEncryptionContext.encrypt("password")),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )
        totpManager.setSanitisedEditResult(Result.success(secret))
        totpManager.setParseResult(Result.success(TotpSpec(secret = secret, label = "label".some())))
        getItemById.emitValue(Result.success(item))

        assertThat(instance.loginItemFormState.primaryTotp)
            .isEqualTo(UIHiddenState.Revealed(TestEncryptionContext.encrypt(secret), secret))
    }

    @Test
    fun `item with totp using non-default parameters shows full URI`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA256&period=10&digits=8"
        val primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(uri), uri)
        val item = TestObserveItems.createItem(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                password = HiddenState.Empty(TestEncryptionContext.encrypt("password")),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )
        totpManager.setSanitisedEditResult(Result.success(uri))
        getItemById.emitValue(Result.success(item))

        assertThat(instance.loginItemFormState.primaryTotp).isEqualTo(UIHiddenState.from(primaryTotp))
    }

    @Test
    fun `if error is InvalidContentFormatVersionError shows right snackbar message`() = runTest {
        updateItem.setResult(Result.failure(InvalidContentFormatVersionError()))

        val item = TestObserveItems.createLogin(shareId = ShareId(SHARE_ID))
        getItemById.emitValue(Result.success(item))

        instance.updateItem(ShareId(SHARE_ID))

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(LoginSnackbarMessages.UpdateAppToUpdateItemError::class.java)
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }

}
