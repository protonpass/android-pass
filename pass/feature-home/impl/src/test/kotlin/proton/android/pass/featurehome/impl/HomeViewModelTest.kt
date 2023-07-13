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

package proton.android.pass.featurehome.impl

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
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.fakes.usecases.TestAddSearchEntry
import proton.android.pass.data.fakes.usecases.TestApplyPendingEvents
import proton.android.pass.data.fakes.usecases.TestClearTrash
import proton.android.pass.data.fakes.usecases.TestDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteItem
import proton.android.pass.data.fakes.usecases.TestDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestRestoreItem
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItem
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.featuresearchoptions.fakes.TestHomeSearchOptionsRepository
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.pass.domain.Item
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class HomeViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: HomeViewModel

    private lateinit var trashItem: TestTrashItem
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var applyPendingEvents: TestApplyPendingEvents
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider
    private lateinit var restoreItem: TestRestoreItem
    private lateinit var restoreItems: TestRestoreItems
    private lateinit var deleteItem: TestDeleteItem
    private lateinit var clearTrash: TestClearTrash
    private lateinit var addSearchEntry: TestAddSearchEntry
    private lateinit var deleteSearchEntry: TestDeleteSearchEntry
    private lateinit var deleteAllSearchEntry: TestDeleteAllSearchEntry
    private lateinit var observeSearchEntry: TestObserveSearchEntry
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var searchOptionsRepository: TestHomeSearchOptionsRepository
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var clock: FixedClock
    private lateinit var observeItems: TestObserveItems
    private lateinit var itemSyncStatusRepository: TestItemSyncStatusRepository
    private lateinit var preferencesRepository: TestPreferenceRepository
    private lateinit var getUserPlan: TestGetUserPlan


    @Before
    fun setup() {
        trashItem = TestTrashItem()
        snackbarDispatcher = TestSnackbarDispatcher()
        clipboardManager = TestClipboardManager()
        applyPendingEvents = TestApplyPendingEvents()
        encryptionContextProvider = TestEncryptionContextProvider()
        restoreItem = TestRestoreItem()
        restoreItems = TestRestoreItems()
        deleteItem = TestDeleteItem()
        clearTrash = TestClearTrash()
        addSearchEntry = TestAddSearchEntry()
        deleteSearchEntry = TestDeleteSearchEntry()
        deleteAllSearchEntry = TestDeleteAllSearchEntry()
        observeSearchEntry = TestObserveSearchEntry()
        telemetryManager = TestTelemetryManager()
        searchOptionsRepository = TestHomeSearchOptionsRepository()
        observeVaults = TestObserveVaults()
        clock = FixedClock(Clock.System.now())
        observeItems = TestObserveItems()
        itemSyncStatusRepository = TestItemSyncStatusRepository()
        preferencesRepository = TestPreferenceRepository()
        getUserPlan = TestGetUserPlan()

        instance = HomeViewModel(
            trashItem = trashItem,
            snackbarDispatcher = snackbarDispatcher,
            clipboardManager = clipboardManager,
            applyPendingEvents = applyPendingEvents,
            encryptionContextProvider = encryptionContextProvider,
            restoreItem = restoreItem,
            restoreItems = restoreItems,
            deleteItem = deleteItem,
            clearTrash = clearTrash,
            addSearchEntry = addSearchEntry,
            deleteSearchEntry = deleteSearchEntry,
            deleteAllSearchEntry = deleteAllSearchEntry,
            observeSearchEntry = observeSearchEntry,
            telemetryManager = telemetryManager,
            homeSearchOptionsRepository = searchOptionsRepository,
            observeVaults = observeVaults,
            clock = clock,
            observeItems = observeItems,
            itemSyncStatusRepository = itemSyncStatusRepository,
            preferencesRepository = preferencesRepository,
            getUserPlan = getUserPlan,
            appDispatchers = TestAppDispatchers()
        )
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
    fun `does not stay in loading if sync finished and search is empty`() = runTest {
        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems = true))
        setupItems()

        instance.onEnterSearch()
        instance.onSearchQueryChange("random")

        instance.homeUiState.test {
            skipItems(2) // Skip initial and loading search state

            val state = awaitItem()
            assertThat(state.homeListUiState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(state.homeListUiState.items).isEmpty()
        }
    }

    @Test
    fun `does not stay in loading if vault switched and has no contents`() = runTest {
        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems = true))

        // Emit initial items
        setupItems()

        // Change vault and emit empty
        instance.setVaultSelection(VaultSelectionOption.Vault(ShareId("random")))
        observeItems.emitValue(emptyList())

        instance.homeUiState.test {
            val state = awaitItem()
            assertThat(state.homeListUiState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(state.homeListUiState.items).isEmpty()
        }
    }

    private suspend fun setupItems(): List<Item> {
        val items = TestObserveItems.defaultValues
            .asList()
            .map {
                it.copy(
                    createTime = clock.now(),
                    modificationTime = clock.now()
                )
            }

        observeItems.emitValue(items)

        preferencesRepository.setUseFaviconsPreference(UseFaviconsPreference.Disabled)

        val vaults = items
            .map { it.shareId }
            .distinct()
            .map { shareId ->
                Vault(
                    shareId = shareId,
                    name = "Vault ${shareId.id}",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    isPrimary = false
                )
            }

        val searchEntries = items.map {
            SearchEntry(
                itemId = it.id,
                shareId = it.shareId,
                userId = UserId("userid"),
                createTime = Clock.System.now().toJavaInstant().epochSecond
            )
        }

        itemSyncStatusRepository.emit(ItemSyncStatus.Synced(hasItems = items.isNotEmpty()))
        observeVaults.sendResult(Result.success(vaults))
        observeItems.emitValue(items)
        observeSearchEntry.emit(searchEntries)

        return items
    }

}
