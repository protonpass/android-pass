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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.commonrust.fakes.passwords.strengths.FakePasswordStrengthCalculator
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.features.itemcreate.common.formprocessor.FakeLoginItemFormProcessor
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.FakeTotpManager

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: FakeTotpManager
    private lateinit var clipboardManager: FakeClipboardManager
    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var baseLoginViewModel: BaseLoginViewModel
    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var draftRepository: DraftRepository
    private lateinit var passwordStrengthCalculator: FakePasswordStrengthCalculator
    private lateinit var emailValidator: FakeEmailValidator

    @Before
    fun setUp() {
        totpManager = FakeTotpManager()
        clipboardManager = FakeClipboardManager()
        observeCurrentUser = FakeObserveCurrentUser().apply { sendUser(TestUser.create()) }
        draftRepository = FakeDraftRepository()
        encryptionContextProvider = FakeEncryptionContextProvider()
        passwordStrengthCalculator = FakePasswordStrengthCalculator()
        emailValidator = FakeEmailValidator()
        baseLoginViewModel = object : BaseLoginViewModel(
            accountManager = FakeAccountManager(),
            snackbarDispatcher = FakeSnackbarDispatcher(),
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            draftRepository = draftRepository,
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = FakeObserveUpgradeInfo(),
            encryptionContextProvider = encryptionContextProvider,
            passwordStrengthCalculator = passwordStrengthCalculator,
            savedStateHandleProvider = FakeSavedStateHandleProvider(),
            emailValidator = emailValidator,
            observeTooltipEnabled = FakeObserveTooltipEnabled(),
            disableTooltip = FakeDisableTooltip(),
            userPreferencesRepository = FakePreferenceRepository(),
            attachmentsHandler = FakeAttachmentHandler(),
            customFieldDraftRepository = CustomFieldDraftRepositoryImpl(),
            customFieldHandler = CustomFieldHandlerImpl(totpManager, encryptionContextProvider),
            loginItemFormProcessor = FakeLoginItemFormProcessor()
        ) {}
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        baseLoginViewModel.onTitleChange(titleInput)
        assertThat(baseLoginViewModel.loginItemFormState.title).isEqualTo(titleInput)
    }

    @Test
    internal fun `GIVEN form is collapsed WHEN email changes THEN email is updated and username is cleared`() =
        runTest {
            val emailInput = "user@email.com"

            baseLoginViewModel.onEmailChanged(emailInput)

            assertThat(baseLoginViewModel.loginItemFormState.email).isEqualTo(emailInput)
            assertThat(baseLoginViewModel.loginItemFormState.username).isEqualTo("")
        }

    @Test
    internal fun `GIVEN form is expanded WHEN email changes THEN state email should be updated`() = runTest {
        val emailInput = "user@email.com"
        baseLoginViewModel.onUsernameOrEmailManuallyExpanded()

        baseLoginViewModel.onEmailChanged(emailInput)

        assertThat(baseLoginViewModel.loginItemFormState.email).isEqualTo(emailInput)
    }

    @Test
    internal fun `GIVEN email validation fails WHEN email changes THEN email is cleared and username is updated`() =
        runTest {
            val emailInput = "invalid email"
            val isValidEmail = false
            emailValidator.setResult(isValidEmail)

            baseLoginViewModel.onEmailChanged(emailInput)

            assertThat(baseLoginViewModel.loginItemFormState.email).isEqualTo("")
            assertThat(baseLoginViewModel.loginItemFormState.username).isEqualTo(emailInput)
        }

    @Test
    internal fun `WHEN username changes THEN state username should be updated`() = runTest {
        val usernameInput = "new username"

        baseLoginViewModel.onUsernameChanged(usernameInput)

        assertThat(baseLoginViewModel.loginItemFormState.username).isEqualTo(usernameInput)
    }

    @Test
    fun `when the password has changed the state should hold it`() = runTest {
        val passwordInput = "Password Changed"
        val encryptedPassword = FakeEncryptionContext.encrypt("Password Changed")
        baseLoginViewModel.onPasswordChange(passwordInput)
        assertThat(baseLoginViewModel.loginItemFormState.password)
            .isEqualTo(UIHiddenState.Revealed(encryptedPassword, passwordInput))
    }

    @Test
    fun `WHEN password changed by user THEN update password strength state`() = runTest {
        val newPasswordValue = TestUtils.randomString()
        val expectedPasswordStrength = PasswordStrength.Strong
        passwordStrengthCalculator.setPasswordStrength(expectedPasswordStrength)

        baseLoginViewModel.onPasswordChange(newPasswordValue)

        assertThat(baseLoginViewModel.loginItemFormState.passwordStrength)
            .isEqualTo(expectedPasswordStrength)
    }

    @Test
    fun `WHEN password changed by automatic generation THEN update password strength state`() {
        val newPasswordValue = encryptionContextProvider.withEncryptionContext {
            encrypt(TestUtils.randomString())
        }
        val expectedPasswordStrength = PasswordStrength.Strong
        passwordStrengthCalculator.setPasswordStrength(expectedPasswordStrength)
        draftRepository.save(DRAFT_PASSWORD_KEY, newPasswordValue)

        assertThat(baseLoginViewModel.loginItemFormState.passwordStrength)
            .isEqualTo(expectedPasswordStrength)
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseLoginViewModel.onNoteChange(noteInput)
        assertThat(baseLoginViewModel.loginItemFormState.note).isEqualTo(noteInput)
    }

    @Test
    fun `when a website has been changed the state should change it`() = runTest {
        val url = "proton.me"
        baseLoginViewModel.onWebsiteChange(url, 0)
        assertThat(baseLoginViewModel.loginItemFormState.urls).isEqualTo(listOf(url))
    }

    @Test
    fun `when a website has been added the state should add it`() = runTest {
        baseLoginViewModel.onAddWebsite()
        assertThat(baseLoginViewModel.loginItemFormState.urls).isEqualTo(listOf("", ""))
    }

    @Test
    fun `when a website has been removed the state should remove it`() = runTest {
        baseLoginViewModel.onRemoveWebsite(0)
        assertThat(baseLoginViewModel.loginItemFormState.urls).isEqualTo(emptyList<String>())
    }
}
