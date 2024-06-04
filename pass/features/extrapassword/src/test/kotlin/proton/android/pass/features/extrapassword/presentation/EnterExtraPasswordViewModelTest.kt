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

package proton.android.pass.features.extrapassword.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.some
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.errors.WrongExtraPasswordException
import proton.android.pass.data.fakes.usecases.accesskey.FakeAuthWithExtraPassword
import proton.android.pass.features.extrapassword.navigation.UserIdNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule

class EnterExtraPasswordViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var instance: EnterExtraPasswordViewModel
    private lateinit var authWithExtraPassword: FakeAuthWithExtraPassword
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        authWithExtraPassword = FakeAuthWithExtraPassword()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = EnterExtraPasswordViewModel(
            authWithExtraPassword = authWithExtraPassword,
            encryptionContextProvider = TestEncryptionContextProvider(),
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = TestSavedStateHandleProvider().apply {
                get()[UserIdNavArgId.key] = USER_ID
            }
        )
    }

    @Test
    fun `emits default state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(ExtraPasswordState.Initial)
        }
    }

    @Test
    fun `emits empty password error`() = runTest {
        instance.onExtraPasswordChanged("")
        instance.onSubmit()
        instance.state.test {
            assertThat(awaitItem().error).isEqualTo(ExtraPasswordError.EmptyPassword.some())
        }
    }

    @Test
    fun `emits wrong password error`() = runTest {
        authWithExtraPassword.setResult(Result.failure(WrongExtraPasswordException()))

        instance.onExtraPasswordChanged("some")
        instance.onSubmit()
        instance.state.test {
            assertThat(awaitItem().error).isEqualTo(ExtraPasswordError.WrongPassword.some())
        }
    }

    @Test
    fun `emits too many failures error`() = runTest {
        authWithExtraPassword.setResult(Result.failure(TooManyExtraPasswordAttemptsException()))

        instance.onExtraPasswordChanged("some")
        instance.onSubmit()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.event).isInstanceOf(EnterExtraPasswordEvent.Logout::class.java)

            val casted = item.event as EnterExtraPasswordEvent.Logout
            assertThat(casted.userId.id).isEqualTo(USER_ID)
        }
    }

    @Test
    fun `emits generic error error`() = runTest {
        authWithExtraPassword.setResult(Result.failure(IllegalStateException("test")))

        instance.onExtraPasswordChanged("some")
        instance.onSubmit()

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isEmpty()).isFalse()

        assertThat(snackbarMessage.value())
            .isInstanceOf(EnterExtraPasswordSnackbarMessage.ExtraPasswordError::class.java)
    }

    companion object {
        private const val USER_ID = "EnterExtraPasswordViewModelTest-userId"
    }

}
