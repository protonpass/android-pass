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
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.fakes.repositories.FakeBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakeChangeAliasStatus
import proton.android.pass.data.fakes.usecases.FakeObserveEncryptedItems
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.FakeAddSearchEntry
import proton.android.pass.data.fakes.usecases.FakeClearTrash
import proton.android.pass.data.fakes.usecases.FakeDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.FakeDeleteItems
import proton.android.pass.data.fakes.usecases.FakeDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveAllShares
import proton.android.pass.data.fakes.usecases.FakeObserveAppNeedsUpdate
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObservePinnedItems
import proton.android.pass.data.fakes.usecases.FakeObserveSearchEntry
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.data.fakes.usecases.FakePinItems
import proton.android.pass.data.fakes.usecases.FakeRestoreAllItems
import proton.android.pass.data.fakes.usecases.FakeRestoreItems
import proton.android.pass.data.fakes.usecases.FakeTrashItems
import proton.android.pass.data.fakes.usecases.FakeUnpinItems
import proton.android.pass.data.fakes.usecases.inappmessages.FakeObserveDeliverableMinimizedPromoInAppMessage
import proton.android.pass.data.fakes.usecases.items.FakeObserveCanCreateItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveEncryptedSharedItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveHasShares
import proton.android.pass.domain.ItemEncrypted
import proton.android.pass.domain.ShareId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.searchoptions.fakes.FakeHomeSearchOptionsRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestUser

internal class HomeViewModelTest {

    @get:Rule
    internal val dispatcher = MainDispatcherRule()

    private lateinit var instance: HomeViewModel

    private lateinit var trashItems: FakeTrashItems
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var clipboardManager: FakeClipboardManager
    private lateinit var performSync: FakePerformSync
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var restoreItems: FakeRestoreItems
    private lateinit var restoreAllItems: FakeRestoreAllItems
    private lateinit var deleteItems: FakeDeleteItems
    private lateinit var clearTrash: FakeClearTrash
    private lateinit var addSearchEntry: FakeAddSearchEntry
    private lateinit var deleteSearchEntry: FakeDeleteSearchEntry
    private lateinit var deleteAllSearchEntry: FakeDeleteAllSearchEntry
    private lateinit var observeSearchEntry: FakeObserveSearchEntry
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var searchOptionsRepository: FakeHomeSearchOptionsRepository
    private lateinit var observeAllShares: FakeObserveAllShares
    private lateinit var clock: FixedClock
    private lateinit var observeEncryptedItems: FakeObserveEncryptedItems
    private lateinit var observePinnedItems: FakeObservePinnedItems
    private lateinit var preferencesRepository: FakePreferenceRepository
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var bulkMoveToVaultRepository: FakeBulkMoveToVaultRepository
    private lateinit var toastManager: FakeToastManager
    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var observeCanCreateItems: FakeObserveCanCreateItems
    private lateinit var observeHasShares: FakeObserveHasShares
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo

    @Before
    internal fun setup() {
        trashItems = FakeTrashItems()
        snackbarDispatcher = FakeSnackbarDispatcher()
        clipboardManager = FakeClipboardManager()
        performSync = FakePerformSync()
        encryptionContextProvider = FakeEncryptionContextProvider()
        restoreItems = FakeRestoreItems()
        restoreAllItems = FakeRestoreAllItems()
        deleteItems = FakeDeleteItems()
        clearTrash = FakeClearTrash()
        addSearchEntry = FakeAddSearchEntry()
        deleteSearchEntry = FakeDeleteSearchEntry()
        deleteAllSearchEntry = FakeDeleteAllSearchEntry()
        observeSearchEntry = FakeObserveSearchEntry()
        telemetryManager = FakeTelemetryManager()
        searchOptionsRepository = FakeHomeSearchOptionsRepository()
        observeAllShares = FakeObserveAllShares()
        clock = FixedClock(Clock.System.now())
        observeEncryptedItems = FakeObserveEncryptedItems()
        preferencesRepository = FakePreferenceRepository()
        getUserPlan = FakeGetUserPlan()
        bulkMoveToVaultRepository = FakeBulkMoveToVaultRepository()
        toastManager = FakeToastManager()
        observePinnedItems = FakeObservePinnedItems()
        observeCurrentUser = FakeObserveCurrentUser().apply { sendUser(TestUser.create()) }
        observeCanCreateItems = FakeObserveCanCreateItems()
        observeHasShares = FakeObserveHasShares()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
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
            pinItems = FakePinItems(),
            unpinItems = FakeUnpinItems(),
            observeAppNeedsUpdate = FakeObserveAppNeedsUpdate(),
            appDispatchers = FakeAppDispatchers(),
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
