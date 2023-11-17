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

package proton.android.pass.featuresharing.impl.sharingpermissions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.featuresharing.impl.EmailNavArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.domain.ShareId

class SharingPermissionsViewModelTest {

    private lateinit var viewModel: SharingPermissionsViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        getVaultById = TestGetVaultById()
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = "my share id"
            get()[EmailNavArgId.key] = "test@example.com"
            get()[SharingWithUserModeArgId.key] = SharingWithUserModeType.ExistingUser.name
        }
        viewModel = SharingPermissionsViewModel(
            getVaultById = getVaultById,
            savedStateHandleProvider = savedStateHandleProvider
        )
    }

    @Test
    fun `test initial state`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.email).isEqualTo("test@example.com")
            assertThat(initialState.vaultName).isNull()
            assertThat(initialState.sharingType).isEqualTo(SharingType.Read)
            assertThat(initialState.event).isEqualTo(SharingPermissionsEvents.Unknown)
        }
    }

    @Test
    fun `test onPermissionChange`() = runTest {
        viewModel.onPermissionChange(SharingType.Write)
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.sharingType).isEqualTo(SharingType.Write)
        }
    }

    @Test
    fun `test onPermissionsSubmit`() = runTest {
        viewModel.onPermissionsSubmit()
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.event).isInstanceOf(SharingPermissionsEvents.NavigateToSummary::class.java)
            val navigationEvent = initialState.event as SharingPermissionsEvents.NavigateToSummary
            assertThat(navigationEvent.shareId).isEqualTo(ShareId("my share id"))
            assertThat(navigationEvent.email).isEqualTo("test@example.com")
            assertThat(navigationEvent.permission).isEqualTo(SharingType.Read.ordinal)
            assertThat(navigationEvent.mode).isEqualTo(SharingWithUserModeType.ExistingUser)
        }
    }

    @Test
    fun `test clearEvent`() = runTest {
        viewModel.clearEvent()
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.event).isEqualTo(SharingPermissionsEvents.Unknown)
        }
    }
}
