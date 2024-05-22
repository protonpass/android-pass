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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.TestAddSearchEntry
import proton.android.pass.data.fakes.usecases.TestClearTrash
import proton.android.pass.data.fakes.usecases.TestDeleteAllSearchEntry
import proton.android.pass.data.fakes.usecases.TestDeleteItems
import proton.android.pass.data.fakes.usecases.TestDeleteSearchEntry
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveAppNeedsUpdate
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObservePinnedItems
import proton.android.pass.data.fakes.usecases.TestObserveSearchEntry
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.fakes.usecases.TestPerformSync
import proton.android.pass.data.fakes.usecases.TestPinItems
import proton.android.pass.data.fakes.usecases.TestRestoreAllItems
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.data.fakes.usecases.TestUnpinItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.featuresearchoptions.fakes.TestHomeSearchOptionsRepository
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.notifications.fakes.TestToastManager
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule

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
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var clock: FixedClock
    private lateinit var observeItems: TestObserveItems
    private lateinit var observePinnedItems: TestObservePinnedItems
    private lateinit var preferencesRepository: TestPreferenceRepository
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var savedState: TestSavedStateHandleProvider
    private lateinit var bulkMoveToVaultRepository: TestBulkMoveToVaultRepository
    private lateinit var toastManager: TestToastManager

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
        observeVaults = TestObserveVaults()
        clock = FixedClock(Clock.System.now())
        observeItems = TestObserveItems()
        preferencesRepository = TestPreferenceRepository()
        getUserPlan = TestGetUserPlan()
        savedState = TestSavedStateHandleProvider()
        bulkMoveToVaultRepository = TestBulkMoveToVaultRepository()
        toastManager = TestToastManager()
        observePinnedItems = TestObservePinnedItems()
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
        observeItems.emitValue(emptyList())

        instance.homeUiState.test {
            val state = awaitItem()
            assertThat(state.homeListUiState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(state.homeListUiState.items).isEmpty()
        }
    }

    @Test
    fun `if savedState contains initial share id it gets passed to repository`() = runTest {
        val shareId = "SHARE_ID"
        savedState.get()[CommonOptionalNavArgId.ShareId.key] = shareId

        createViewModel()
        val vaultSelection = searchOptionsRepository.observeVaultSelectionOption().first()
        assertThat(vaultSelection).isEqualTo(VaultSelectionOption.Vault(ShareId(shareId)))
    }

    private fun setupItems(): List<Item> {
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
                    icon = ShareIcon.Icon1
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

        observeVaults.sendResult(Result.success(vaults))
        observeItems.emitValue(items)
        observeSearchEntry.emit(searchEntries)

        return items
    }

    @Test
    internal fun `WHEN read only item is selected THEN show toast message`() {
        instance.onReadOnlyItemSelected()

        assertThat(toastManager.stringResourceMessage).isEqualTo(R.string.home_toast_items_selected_read_only)
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
            observeVaults = observeVaults,
            clock = clock,
            observeItems = observeItems,
            observePinnedItems = observePinnedItems,
            preferencesRepository = preferencesRepository,
            getUserPlan = getUserPlan,
            savedState = savedState,
            bulkMoveToVaultRepository = bulkMoveToVaultRepository,
            toastManager = toastManager,
            pinItem = FakePinItem(),
            unpinItem = FakeUnpinItem(),
            pinItems = TestPinItems(),
            unpinItems = TestUnpinItems(),
            observeAppNeedsUpdate = TestObserveAppNeedsUpdate()
        )
    }

}
