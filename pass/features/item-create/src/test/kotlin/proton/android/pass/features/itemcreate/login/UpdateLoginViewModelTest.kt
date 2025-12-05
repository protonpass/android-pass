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
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.commonrust.fakes.passwords.strengths.FakePasswordStrengthCalculator
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentLinkRepository
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeCreateAlias
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeUpdateItem
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.attachments.FakeRenameAttachments
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.data.fakes.work.FakeWorkerLauncher
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.features.itemcreate.common.formprocessor.FakeLoginItemFormProcessor
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.api.TotpSpec
import proton.android.pass.totp.fakes.FakeTotpManager

class UpdateLoginViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: UpdateLoginViewModel

    private lateinit var getItemById: FakeGetItemById
    private lateinit var totpManager: FakeTotpManager
    private lateinit var updateItem: FakeUpdateItem
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var observeShare: FakeObserveShare
    private lateinit var observeItemById: FakeObserveItemById
    private lateinit var settingsRepository: FakeInternalSettingsRepository

    @Before
    fun setup() {
        getItemById = FakeGetItemById()
        totpManager = FakeTotpManager()
        updateItem = FakeUpdateItem()
        snackbarDispatcher = FakeSnackbarDispatcher()
        encryptionContextProvider = FakeEncryptionContextProvider()
        observeShare = FakeObserveShare()
        observeItemById = FakeObserveItemById()
        settingsRepository = FakeInternalSettingsRepository()
    }

    private fun createInstance(): UpdateLoginViewModel = UpdateLoginViewModel(
        getItemById = getItemById,
        accountManager = FakeAccountManager().apply {
            sendPrimaryUserId(UserId("UserId"))
        },
        clipboardManager = FakeClipboardManager(),
        totpManager = totpManager,
        snackbarDispatcher = snackbarDispatcher,
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonOptionalNavArgId.ShareId.key] = SHARE_ID
            get()[CommonNavArgId.ItemId.key] = ITEM_ID
        },
        encryptionContextProvider = encryptionContextProvider,
        passwordStrengthCalculator = FakePasswordStrengthCalculator(),
        observeCurrentUser = FakeObserveCurrentUser().apply { sendUser(TestUser.create()) },
        telemetryManager = FakeTelemetryManager(),
        draftRepository = FakeDraftRepository(),
        observeUpgradeInfo = FakeObserveUpgradeInfo(),
        updateItem = updateItem,
        createAlias = FakeCreateAlias(),
        emailValidator = FakeEmailValidator(),
        observeTooltipEnabled = FakeObserveTooltipEnabled(),
        disableTooltip = FakeDisableTooltip(),
        userPreferencesRepository = FakePreferenceRepository(),
        workerLauncher = FakeWorkerLauncher(),
        attachmentsHandler = FakeAttachmentHandler(),
        linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
        renameAttachments = FakeRenameAttachments(),
        customFieldDraftRepository = CustomFieldDraftRepositoryImpl(),
        pendingAttachmentLinkRepository = FakePendingAttachmentLinkRepository(),
        customFieldHandler = CustomFieldHandlerImpl(FakeTotpManager(), encryptionContextProvider),
        loginItemFormProcessor = FakeLoginItemFormProcessor(),
        observeShare = observeShare,
        settingsRepository = settingsRepository,
        observeItemById = observeItemById
    )

    @Test
    fun `item with totp using default parameters shows only secret`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA1&period=30&digits=6"
        val primaryTotp = HiddenState.Revealed(FakeEncryptionContext.encrypt(uri), uri)
        val item = TestItem.create(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                password = HiddenState.Empty(FakeEncryptionContext.encrypt("password")),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )
        totpManager.setSanitisedEditResult(Result.success(secret))
        totpManager.setParseResult(Result.success(TotpSpec(secret = secret, label = "label".some())))
        getItemById.emit(Result.success(item))
        instance = createInstance()

        assertThat(instance.loginItemFormState.primaryTotp)
            .isEqualTo(UIHiddenState.Revealed(FakeEncryptionContext.encrypt(secret), secret))
    }

    @Test
    fun `item with totp using non-default parameters shows full URI`() = runTest {
        val secret = "secret"
        val uri = "otpauth://totp/label?secret=$secret&algorithm=SHA256&period=10&digits=8"
        val primaryTotp = HiddenState.Revealed(FakeEncryptionContext.encrypt(uri), uri)
        val item = TestItem.create(
            itemContents = ItemContents.Login(
                title = "item",
                note = "note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                password = HiddenState.Empty(FakeEncryptionContext.encrypt("password")),
                urls = emptyList(),
                packageInfoSet = emptySet(),
                primaryTotp = primaryTotp,
                customFields = emptyList(),
                passkeys = emptyList()
            )
        )
        totpManager.setSanitisedEditResult(Result.success(uri))
        getItemById.emit(Result.success(item))
        instance = createInstance()

        assertThat(instance.loginItemFormState.primaryTotp).isEqualTo(UIHiddenState.from(primaryTotp))
    }

    @Test
    fun `if error is InvalidContentFormatVersionError shows right snackbar message`() = runTest {
        updateItem.setResult(Result.failure(InvalidContentFormatVersionError()))

        val item = TestItem.createLogin(shareId = ShareId(SHARE_ID))
        getItemById.emit(Result.success(item))
        instance = createInstance()

        instance.updateItem(ShareId(SHARE_ID))

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(LoginSnackbarMessages.UpdateAppToUpdateItemError::class.java)
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }

}
