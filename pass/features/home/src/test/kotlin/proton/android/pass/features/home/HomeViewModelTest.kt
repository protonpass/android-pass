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

package proton.android.pass.features.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.fakes.TestAppDispatchers
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeChangeAliasStatus
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.TestAddSearchEntry
import proton.android.pass.data.fakes.usecases.TestClearTrash
import proton.android.pass.data.fakes.usecases.TestDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteItems
import proton.android.pass.data.fakes.usecases.TestDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveAllShares
import proton.android.pass.data.fakes.usecases.TestObserveAppNeedsUpdate
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObservePinnedItems
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestPerformSync
import proton.android.pass.data.fakes.usecases.TestPinItems
import proton.android.pass.data.fakes.usecases.TestRestoreAllItems
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.data.fakes.usecases.TestUnpinItems
import proton.android.pass.data.fakes.usecases.inappmessages.FakeObserveDeliverableMinimizedPromoInAppMessage
import proton.android.pass.data.fakes.usecases.items.FakeObserveCanCreateItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveEncryptedSharedItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveHasShares
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ShareId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.notifications.fakes.TestToastManager
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.searchoptions.fakes.TestHomeSearchOptionsRepository
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestUser

internal class HomeViewModelTest {

    @get:Rule
    internal val dispatcher = MainDispatcherRule()

    private lateinit var instance: HomeViewModel

    private lateinit var trashItems: TestTrashItems
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var performSync: TestPerformSync
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider
    private lateinit var restoreItems: TestRestoreItems
    private lateinit var restoreAllItems: TestRestoreAllItems
    private lateinit var deleteItems: TestDeleteItems
    private lateinit var clearTrash: TestClearTrash
    private lateinit var addSearchEntry: TestAddSearchEntry
    private lateinit var deleteSearchEntry: TestDeleteSearchEntry
    private lateinit var deleteAllSearchEntry: TestDeleteAllSearchEntry
    private lateinit var observeSearchEntry: TestObserveSearchEntry
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var searchOptionsRepository: TestHomeSearchOptionsRepository
    private lateinit var observeAllShares: TestObserveAllShares
    private lateinit var clock: FixedClock
    private lateinit var observeEncryptedItems: FakeObserveEncryptedItems
    private lateinit var observePinnedItems: TestObservePinnedItems
    private lateinit var preferencesRepository: TestPreferenceRepository
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var bulkMoveToVaultRepository: TestBulkMoveToVaultRepository
    private lateinit var toastManager: TestToastManager
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var observeCanCreateItems: FakeObserveCanCreateItems
    private lateinit var observeHasShares: FakeObserveHasShares
    private lateinit var observeUpgradeInfo: TestObserveUpgradeInfo

    @Before
    internal fun setup() {
        trashItems = TestTrashItems()
        snackbarDispatcher = TestSnackbarDispatcher()
        clipboardManager = TestClipboardManager()
        performSync = TestPerformSync()
        encryptionContextProvider = TestEncryptionContextProvider()
        restoreItems = TestRestoreItems()
        restoreAllItems = TestRestoreAllItems()
        deleteItems = TestDeleteItems()
        clearTrash = TestClearTrash()
        addSearchEntry = TestAddSearchEntry()
        deleteSearchEntry = TestDeleteSearchEntry()
        deleteAllSearchEntry = TestDeleteAllSearchEntry()
        observeSearchEntry = TestObserveSearchEntry()
        telemetryManager = TestTelemetryManager()
        searchOptionsRepository = TestHomeSearchOptionsRepository()
        observeAllShares = TestObserveAllShares()
        clock = FixedClock(Clock.System.now())
        observeEncryptedItems = FakeObserveEncryptedItems()
        preferencesRepository = TestPreferenceRepository()
        getUserPlan = TestGetUserPlan()
        bulkMoveToVaultRepository = TestBulkMoveToVaultRepository()
        toastManager = TestToastManager()
        observePinnedItems = TestObservePinnedItems()
        observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        observeCanCreateItems = FakeObserveCanCreateItems()
        observeHasShares = FakeObserveHasShares()
        observeUpgradeInfo = TestObserveUpgradeInfo()
        createViewModel()
    }

    @Test
    fun `emits Loading as initial state`() = runTest {
        instance.homeUiState.test {
            assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
        }
    }

    @Test
    fun `emits items`() = runTest {
        val items = setupItems()

        instance.homeUiState.test {
            val state = awaitItem()
            assertThat(state.homeListUiState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            val expected = persistentListOf(
                GroupedItemList(
                    key = GroupingKeys.MostRecentKey(
                        formatResultKey = DateFormatUtils.Format.Today,
                        instant = clock.now()
                    ),
                    items = items.map {
                        encryptionContextProvider.withEncryptionContext {
                            it.toUiModel(this@withEncryptionContext)
                        }
                    }
                )
            )
            assertThat(state.homeListUiState.items).isEqualTo(expected)
        }
    }

    @Test
    fun `does not stay in loading if vault switched and has no contents`() = runTest {
        // Emit initial items
        setupItems()

        // Change vault and emit empty
        instance.setVaultSelection(VaultSelectionOption.Vault(ShareId("random")))
        observeEncryptedItems.emitValue(emptyList())

        instance.homeUiState.test {
            val state = awaitItem()
            assertThat(state.homeListUiState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(state.homeListUiState.items).isEmpty()
        }
    }

    private fun setupItems(): List<ItemEncrypted> {
        val items = FakeObserveEncryptedItems.defaultValues
            .asList()
            .map {
                it.copy(
                    createTime = clock.now(),
                    modificationTime = clock.now()
                )
            }

        observeEncryptedItems.emitValue(items)

        preferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.Disabled)

        val vaultShares = items
            .map { it.shareId }
            .distinct()
            .map { shareId -> TestShare.Vault.create(id = shareId.id) }

        val searchEntries = items.map {
            SearchEntry(
                itemId = it.id,
                shareId = it.shareId,
                userId = UserId("userid"),
                createTime = Clock.System.now().toJavaInstant().epochSecond
            )
        }
        observeAllShares.sendResult(Result.success(vaultShares))
        observeEncryptedItems.emitValue(items)
        observeSearchEntry.emit(searchEntries)
        observeCanCreateItems.emit(canCreateItems = true)
        observeHasShares.emit(hasShares = true)

        return items
    }

    @Test
    internal fun `WHEN read only item is selected THEN show toast message`() {
        instance.onReadOnlyItemSelected()

        assertThat(toastManager.stringResourceMessage).isEqualTo(R.string.home_toast_items_selected_read_only)
    }

    @Test
    internal fun `WHEN item shared with me is selected THEN show toast message`() {
        instance.onSharedWithMeItemSelected()

        assertThat(toastManager.stringResourceMessage).isEqualTo(R.string.home_toast_items_selected_shared_with_me)
    }

    private fun createViewModel() {
        instance = HomeViewModel(
            trashItems = trashItems,
            snackbarDispatcher = snackbarDispatcher,
            clipboardManager = clipboardManager,
            performSync = performSync,
            encryptionContextProvider = encryptionContextProvider,
            restoreItems = restoreItems,
            restoreAllItems = restoreAllItems,
            deleteItem = deleteItems,
            clearTrash = clearTrash,
            addSearchEntry = addSearchEntry,
            deleteSearchEntry = deleteSearchEntry,
            deleteAllSearchEntry = deleteAllSearchEntry,
            observeSearchEntry = observeSearchEntry,
            telemetryManager = telemetryManager,
            homeSearchOptionsRepository = searchOptionsRepository,
            observeAllShares = observeAllShares,
            clock = clock,
            observeEncryptedItems = observeEncryptedItems,
            observePinnedItems = observePinnedItems,
            preferencesRepository = preferencesRepository,
            getUserPlan = getUserPlan,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            toastManager = toastManager,
            pinItem = FakePinItem(),
            unpinItem = FakeUnpinItem(),
            pinItems = TestPinItems(),
            unpinItems = TestUnpinItems(),
            observeAppNeedsUpdate = TestObserveAppNeedsUpdate(),
            appDispatchers = TestAppDispatchers(),
            observeCurrentUser = observeCurrentUser,
            changeAliasStatus = FakeChangeAliasStatus(),
            observeEncryptedSharedItems = FakeObserveEncryptedSharedItems(),
            observeCanCreateItems = observeCanCreateItems,
            observeHasShares = observeHasShares,
            observeDeliverableMinimizedPromoInAppMessages = FakeObserveDeliverableMinimizedPromoInAppMessage()
                .apply { emitPromoMessage(null) },
            observeUpgradeInfo = observeUpgradeInfo
        )
    }
}
