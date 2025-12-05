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

package proton.android.pass.features.sharing.sharingsummary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.fakes.repositories.FakeBulkInviteRepository
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.FakeInviteToVault
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.invites.FakeInviteToItem
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.sharing.SharingSnackbarMessage.InviteSentError
import proton.android.pass.features.sharing.SharingSnackbarMessage.InviteSentSuccess
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.VaultTestFactory

internal class SharingSummaryViewModelTest {

    private lateinit var getVaultWithItemCountById: FakeGetVaultWithItemCountById
    private lateinit var inviteToVault: FakeInviteToVault
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var bulkInviteRepository: FakeBulkInviteRepository
    private lateinit var observeItemById: FakeObserveItemById
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var userPreferencesRepository: FakePreferenceRepository
    private lateinit var inviteToItem: FakeInviteToItem

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        getVaultWithItemCountById = FakeGetVaultWithItemCountById()
        inviteToVault = FakeInviteToVault()
        snackbarDispatcher = FakeSnackbarDispatcher()
        bulkInviteRepository = FakeBulkInviteRepository().apply {
            runBlocking { storeInvites(listOf(UserTarget(TEST_EMAIL, ShareRole.Read))) }
        }

        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = TEST_SHARE_ID
        }
        observeItemById = FakeObserveItemById()
        encryptionContextProvider = FakeEncryptionContextProvider()
        userPreferencesRepository = FakePreferenceRepository()
        inviteToItem = FakeInviteToItem()
    }

    @Test
    fun `GIVEN vault is sharing WHEN view model is initialized THEN initial state is emitted`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val expectedState = SharingSummaryState.Initial

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault is sharing WHEN required data is loaded THEN ShareVault state is emitted`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val vaultData = createVaultWithItemCount()
        val expectedState = SharingSummaryState.ShareVault(
            event = SharingSummaryEvent.Idle,
            inviteTargets = listOf(UserTarget(TEST_EMAIL, ShareRole.Read)),
            isLoadingState = IsLoadingState.NotLoading,
            vaultWithItemCount = vaultData,
            isRenameAdminToManagerEnabled = false
        )

        getVaultWithItemCountById.emitValue(vaultData)

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN vault is sharing WHEN there are no addresses to share with THEN navigate home`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val expectedEvent = SharingSummaryEvent.OnGoHome

        getVaultWithItemCountById.emitValue(createVaultWithItemCount())
        bulkInviteRepository.clear()

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state.event).isEqualTo(expectedEvent)
        }
    }

    @Test
    fun `GIVEN vault is sharing WHEN error occurs while inviting THEN error message is shown`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val expectedMessage = InviteSentError
        inviteToVault.setResult(Result.failure(RuntimeException("test exception")))

        viewModel.onShareVault()

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN vault is sharing WHEN no errors occur while inviting THEN success message is shown`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val expectedMessage = InviteSentSuccess
        inviteToVault.setResult(Result.success(Unit))

        viewModel.onShareVault()

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN item is sharing WHEN view model is initialized THEN initial state is emitted`() = runTest {
        val viewModel = createViewModel(isItemSharing = true)
        val expectedState = SharingSummaryState.Initial

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item is sharing WHEN there are no addresses to share with THEN navigate home`() = runTest {
        val viewModel = createViewModel(isItemSharing = true)
        val item = ItemTestFactory.create(
            itemContents = ItemContents.Note(
                title = "item",
                note = "note",
                customFields = emptyList()
            )
        )
        val expectedEvent = SharingSummaryEvent.OnGoHome

        observeItemById.emitValue(Result.success(item))
        userPreferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.Enabled)
        bulkInviteRepository.clear()

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state.event).isEqualTo(expectedEvent)
        }
    }

    @Test
    fun `GIVEN item is sharing WHEN error occurs while inviting THEN error message is shown`() = runTest {
        val viewModel = createViewModel(isItemSharing = true)
        val expectedMessage = InviteSentError
        inviteToItem.setResult(shouldFail = true)

        viewModel.onShareItem(ItemId("testId"), ItemCategory.Note)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    @Test
    fun `GIVEN item is sharing WHEN required data is loaded THEN ShareItem state is emitted`() = runTest {
        val viewModel = createViewModel(isItemSharing = true)
        savedStateHandleProvider.apply {
            get()[CommonOptionalNavArgId.ItemId.key] = TEST_ITEM_ID
        }
        val item = ItemTestFactory.create(
            itemContents = ItemContents.Note(
                title = "item",
                note = "note",
                customFields = emptyList()
            )
        )
        val useFaviconsPreference = UseFaviconsPreference.Enabled

        val expectedState = SharingSummaryState.ShareItem(
            event = SharingSummaryEvent.Idle,
            inviteTargets = listOf(UserTarget(TEST_EMAIL, ShareRole.Read)),
            isLoadingState = IsLoadingState.NotLoading,
            itemUiModel = encryptionContextProvider.withEncryptionContext {
                item.toUiModel(this@withEncryptionContext)
            },
            useFaviconsPreference = useFaviconsPreference,
            isRenameAdminToManagerEnabled = false
        )
        userPreferencesRepository.setUseFaviconsPreference(useFaviconsPreference)
        observeItemById.emitValue(Result.success(item))

        viewModel.stateFlow.test {
            val state = awaitItem()

            assertThat(state).isEqualTo(expectedState)
        }
    }

    @Test
    fun `GIVEN item is sharing WHEN no errors occur while inviting THEN success message is shown`() = runTest {
        val viewModel = createViewModel(isItemSharing = false)
        val expectedMessage = InviteSentSuccess

        viewModel.onShareItem(ItemId("testId"), ItemCategory.Note)

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(expectedMessage)
    }

    private fun createViewModel(isItemSharing: Boolean): SharingSummaryViewModel {
        savedStateHandleProvider.apply {
            get()[CommonOptionalNavArgId.ItemId.key] = TEST_ITEM_ID.takeIf { isItemSharing }
        }

        return SharingSummaryViewModel(
            getVaultWithItemCountById = getVaultWithItemCountById,
            inviteToVault = inviteToVault,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = savedStateHandleProvider,
            bulkInviteRepository = bulkInviteRepository,
            getUserPlan = FakeGetUserPlan(),
            observeItemById = observeItemById,
            inviteToItem = inviteToItem,
            encryptionContextProvider = encryptionContextProvider,
            userPreferencesRepository = userPreferencesRepository,
            featureFlagsPreferencesRepository = FakeFeatureFlagsPreferenceRepository()
        )
    }

    private fun createVaultWithItemCount() = VaultWithItemCount(
        vault = VaultTestFactory.create(
            shareId = ShareId(id = TEST_SHARE_ID)
        ),
        activeItemCount = 5521,
        trashedItemCount = 6902
    )

    private companion object {

        private const val TEST_ITEM_ID = "SharingSummaryViewModelTest-ItemID"

        private const val TEST_SHARE_ID = "SharingSummaryViewModelTest-ShareID"

        private const val TEST_EMAIL = "myemail@proton.me"

    }

}
