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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.fakes.repositories.TestBulkInviteRepository
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule

class SharingPermissionsViewModelTest {

    private lateinit var viewModel: SharingPermissionsViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var savedStateHandleProvider: TestSavedStateHandleProvider
    private lateinit var bulkInviteRepository: TestBulkInviteRepository

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        getVaultById = TestGetVaultById()
        bulkInviteRepository = TestBulkInviteRepository().apply {
            runBlocking { storeAddresses(listOf(TEST_EMAIL)) }
        }
        savedStateHandleProvider = TestSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = TEST_SHARE_ID
        }
        viewModel = SharingPermissionsViewModel(
            getVaultById = getVaultById,
            savedStateHandleProvider = savedStateHandleProvider,
            bulkInviteRepository = bulkInviteRepository
        )
    }

    @Test
    fun `test initial state`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.email).isEqualTo(TEST_EMAIL)
            assertThat(initialState.vaultName).isNull()
            assertThat(initialState.sharingType).isEqualTo(SharingType.Read)
            assertThat(initialState.event).isEqualTo(SharingPermissionsEvents.Unknown)
        }
    }

    @Test
    fun `test onPermissionChange`() = runTest {
        viewModel.onPermissionChange(TEST_EMAIL, SharingType.Write)
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
            assertThat(navigationEvent.shareId).isEqualTo(ShareId(TEST_SHARE_ID))
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

    companion object {
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_SHARE_ID = "SharingPermissionsViewModelTest-ShareID"
    }
}
