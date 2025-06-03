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

package proton.android.pass.features.itemcreate.creditcard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class BaseCreditCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: BaseCreditCardViewModel
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction
    private lateinit var featureFlagsRepository: TestFeatureFlagsPreferenceRepository

    @Before
    fun setUp() {
        featureFlagsRepository = TestFeatureFlagsPreferenceRepository()
        canPerformPaidAction = TestCanPerformPaidAction().apply {
            setResult(true)
        }
        instance = object : BaseCreditCardViewModel(
            encryptionContextProvider = TestEncryptionContextProvider(),
            canPerformPaidAction = canPerformPaidAction,
            featureFlagsRepository = featureFlagsRepository,
            savedStateHandleProvider = TestSavedStateHandleProvider(),
            attachmentsHandler = FakeAttachmentHandler(),
            userPreferencesRepository = TestPreferenceRepository(),
            customFieldHandler = CustomFieldHandlerImpl(TestEncryptionContextProvider()),
            customFieldDraftRepository = CustomFieldDraftRepositoryImpl()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        canPerformPaidAction.setResult(false)
        instance.baseState.test {
            assertThat(awaitItem()).isEqualTo(BaseCreditCardUiState.Initial)
        }
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        instance.onTitleChange(titleInput)
        assertThat(instance.creditCardItemFormState.title).isEqualTo(titleInput)
    }

    @Test
    fun `when the cardholder has changed the state should hold it`() = runTest {
        val cardHolder = "Username Changed"
        instance.onNameChanged(cardHolder)
        assertThat(instance.creditCardItemFormState.cardHolder).isEqualTo(cardHolder)
    }

    @Test
    fun `when the number has changed the state should hold it`() = runTest {
        val number = "123456789"
        instance.onNumberChanged(number)
        assertThat(instance.creditCardItemFormState.number).isEqualTo(number)
    }

    @Test
    fun `when the pin has changed the state should hold it`() = runTest {
        val pin = "7894"
        val encryptedPin = TestEncryptionContext.encrypt(pin)
        instance.onPinChanged(pin)
        assertThat(instance.creditCardItemFormState.pin)
            .isEqualTo(UIHiddenState.Revealed(encryptedPin, pin))
    }

    @Test
    fun `when the cvv has changed the state should hold it`() = runTest {
        val cvv = "7894"
        val encryptedCVV = TestEncryptionContext.encrypt(cvv)
        instance.onCVVChanged(cvv)
        assertThat(instance.creditCardItemFormState.cvv)
            .isEqualTo(UIHiddenState.Revealed(encryptedCVV, cvv))
    }

    @Test
    fun `cannot enter a pin with more than max digits`() = runTest {
        val rightPin = "1".repeat(BaseCreditCardViewModel.PIN_MAX_LENGTH)
        val tooLongPin = "${rightPin}1"
        val encryptedPin = TestEncryptionContext.encrypt(rightPin)
        instance.onPinChanged(rightPin)
        instance.onPinChanged(tooLongPin)
        assertThat(instance.creditCardItemFormState.pin)
            .isEqualTo(UIHiddenState.Revealed(encryptedPin, rightPin))
    }

    @Test
    fun `cannot enter a cvv with more than max digits`() = runTest {
        val rightCvv = "1".repeat(BaseCreditCardViewModel.CVV_MAX_LENGTH)
        val tooLongCvv = "${rightCvv}1"
        val encryptedCvv = TestEncryptionContext.encrypt(rightCvv)
        instance.onCVVChanged(rightCvv)
        instance.onCVVChanged(tooLongCvv)
        assertThat(instance.creditCardItemFormState.cvv)
            .isEqualTo(UIHiddenState.Revealed(encryptedCvv, rightCvv))
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        instance.onNoteChanged(noteInput)
        assertThat(instance.creditCardItemFormState.note).isEqualTo(noteInput)
    }

    @Test
    fun `when the cvv gets focus it changes the visibility`() = runTest {
        val cvv = "7894"
        assertThat(instance.creditCardItemFormState.cvv).isInstanceOf(UIHiddenState.Empty::class.java)
        instance.onCVVChanged(cvv)
        assertThat(instance.creditCardItemFormState.cvv).isInstanceOf(UIHiddenState.Revealed::class.java)
        instance.onCVVFocusChanged(false)
        assertThat(instance.creditCardItemFormState.cvv).isInstanceOf(UIHiddenState.Concealed::class.java)

        instance.onCVVChanged("")
        instance.onCVVFocusChanged(false)
        assertThat(instance.creditCardItemFormState.cvv).isInstanceOf(UIHiddenState.Empty::class.java)
    }

    @Test
    fun `when the pin gets focus it changes the visibility`() = runTest {
        val pin = "1235"
        assertThat(instance.creditCardItemFormState.pin).isInstanceOf(UIHiddenState.Empty::class.java)
        instance.onPinChanged(pin)
        assertThat(instance.creditCardItemFormState.pin).isInstanceOf(UIHiddenState.Revealed::class.java)
        instance.onPinFocusChanged(false)
        assertThat(instance.creditCardItemFormState.pin).isInstanceOf(UIHiddenState.Concealed::class.java)
        instance.onPinChanged("")
        instance.onPinFocusChanged(false)
        assertThat(instance.creditCardItemFormState.pin).isInstanceOf(UIHiddenState.Empty::class.java)
    }

    @Test
    fun `given a long expiration date the state should hold the correct`() = runTest {
        val rightExpirationDate = "1".repeat(BaseCreditCardViewModel.EXPIRATION_DATE_MAX_LENGTH)
        val tooLongDate = "${rightExpirationDate}1"
        instance.onExpirationDateChanged(tooLongDate)
        assertThat(instance.creditCardItemFormState.expirationDate).isEqualTo(rightExpirationDate)
    }
}
