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

package proton.android.pass.featureitemcreate.impl.creditcard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.HiddenState

class BaseCreditCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: BaseCreditCardViewModel
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction

    private val initial = BaseCreditCardUiState.default(
        cvv = HiddenState.Empty(TestEncryptionContext.encrypt("")),
        pin = HiddenState.Empty(TestEncryptionContext.encrypt(""))
    )

    @Before
    fun setUp() {
        canPerformPaidAction = TestCanPerformPaidAction().apply {
            setResult(true)
        }
        instance = object : BaseCreditCardViewModel(
            encryptionContextProvider = TestEncryptionContextProvider(),
            canPerformPaidAction = canPerformPaidAction
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        instance.baseState.test {
            assertThat(awaitItem()).isEqualTo(initial)
        }
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        instance.onTitleChange(titleInput)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(title = titleInput))
        }
    }

    @Test
    fun `when the cardholder has changed the state should hold it`() = runTest {
        val cardHolder = "Username Changed"
        instance.onNameChanged(cardHolder)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(cardHolder = cardHolder))
        }
    }

    @Test
    fun `when the number has changed the state should hold it`() = runTest {
        val number = "123456789"
        instance.onNumberChanged(number)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(number = number))
        }
    }

    @Test
    fun `when the pin has changed the state should hold it`() = runTest {
        val pin = "7894"
        val encryptedPin = TestEncryptionContext.encrypt(pin)
        instance.onPinChanged(pin)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(
                    initial.contents.copy(
                        pin = HiddenState.Revealed(encryptedPin, pin)
                    )
                )
        }
    }

    @Test
    fun `when the cvv has changed the state should hold it`() = runTest {
        val cvv = "7894"
        val encryptedPin = TestEncryptionContext.encrypt(cvv)
        instance.onCVVChanged(cvv)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(
                    initial.contents.copy(
                        cvv = HiddenState.Revealed(encryptedPin, cvv)
                    )
                )
        }
    }

    @Test
    fun `cannot enter a pin with more than max digits`() = runTest {
        val rightPin = "1".repeat(BaseCreditCardViewModel.PIN_MAX_LENGTH)
        val tooLongPin = "${rightPin}1"
        val encryptedPin = TestEncryptionContext.encrypt(rightPin)
        instance.onPinChanged(rightPin)
        instance.onPinChanged(tooLongPin)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(
                    initial.contents.copy(
                        pin = HiddenState.Revealed(encryptedPin, rightPin)
                    )
                )
        }
    }

    @Test
    fun `cannot enter a cvv with more than max digits`() = runTest {
        val rightCvv = "1".repeat(BaseCreditCardViewModel.CVV_MAX_LENGTH)
        val tooLongCvv = "${rightCvv}1"
        val encryptedCvv = TestEncryptionContext.encrypt(rightCvv)
        instance.onCVVChanged(rightCvv)
        instance.onCVVChanged(tooLongCvv)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(
                    initial.contents.copy(
                        cvv = HiddenState.Revealed(encryptedCvv, rightCvv)
                    )
                )
        }
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        instance.onNoteChanged(noteInput)
        instance.baseState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(note = noteInput))
        }
    }

    @Test
    fun `when the cvv gets focus it changes the visibility`() = runTest {
        val cvv = "7894"
        instance.baseState.test {
            assertThat(awaitItem().contents.cvv).isInstanceOf(HiddenState.Empty::class.java)
        }
        instance.onCVVChanged(cvv)
        instance.baseState.test {
            assertThat(awaitItem().contents.cvv).isInstanceOf(HiddenState.Revealed::class.java)
        }

        instance.onCVVFocusChanged(false)
        instance.baseState.test {
            assertThat(awaitItem().contents.cvv).isInstanceOf(HiddenState.Concealed::class.java)
        }

        instance.onCVVChanged("")
        instance.onCVVFocusChanged(false)
        instance.baseState.test {
            assertThat(awaitItem().contents.cvv).isInstanceOf(HiddenState.Empty::class.java)
        }
    }

    @Test
    fun `when the pin gets focus it changes the visibility`() = runTest {
        val pin = "1235"
        instance.baseState.test {
            assertThat(awaitItem().contents.pin).isInstanceOf(HiddenState.Empty::class.java)
        }
        instance.onPinChanged(pin)
        instance.baseState.test {
            assertThat(awaitItem().contents.pin).isInstanceOf(HiddenState.Revealed::class.java)
        }

        instance.onPinFocusChanged(false)
        instance.baseState.test {
            assertThat(awaitItem().contents.pin).isInstanceOf(HiddenState.Concealed::class.java)
        }

        instance.onPinChanged("")
        instance.onPinFocusChanged(false)
        instance.baseState.test {
            assertThat(awaitItem().contents.pin).isInstanceOf(HiddenState.Empty::class.java)
        }
    }

    @Test
    fun `emits downgraded mode if cannot perform paid action`() = runTest {
        canPerformPaidAction.setResult(false)
        instance.baseState.test {
            assertThat(awaitItem().isDowngradedMode).isTrue()
        }
    }
}
