/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemcreate.impl.login

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.fakes.TestEmailValidator
import proton.android.pass.commonrust.fakes.passwords.strengths.TestPasswordStrengthCalculator
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.featureitemcreate.impl.attachments.FakeAttachmentHandler
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var baseLoginViewModel: BaseLoginViewModel
    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var draftRepository: DraftRepository
    private lateinit var passwordStrengthCalculator: TestPasswordStrengthCalculator
    private lateinit var emailValidator: TestEmailValidator
    private lateinit var featureFlagsPreferenceRepository: TestFeatureFlagsPreferenceRepository

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        draftRepository = TestDraftRepository()
        encryptionContextProvider = TestEncryptionContextProvider()
        passwordStrengthCalculator = TestPasswordStrengthCalculator()
        emailValidator = TestEmailValidator()
        featureFlagsPreferenceRepository = TestFeatureFlagsPreferenceRepository()
        baseLoginViewModel = object : BaseLoginViewModel(
            accountManager = TestAccountManager(),
            snackbarDispatcher = TestSnackbarDispatcher(),
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            draftRepository = draftRepository,
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = TestObserveUpgradeInfo(),
            encryptionContextProvider = encryptionContextProvider,
            passwordStrengthCalculator = passwordStrengthCalculator,
            savedStateHandleProvider = TestSavedStateHandleProvider(),
            emailValidator = emailValidator,
            observeTooltipEnabled = FakeObserveTooltipEnabled(),
            disableTooltip = FakeDisableTooltip(),
            userPreferencesRepository = TestPreferenceRepository(),
            featureFlagsRepository = featureFlagsPreferenceRepository,
            attachmentsHandler = FakeAttachmentHandler()
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
        val encryptedPassword = TestEncryptionContext.encrypt("Password Changed")
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
