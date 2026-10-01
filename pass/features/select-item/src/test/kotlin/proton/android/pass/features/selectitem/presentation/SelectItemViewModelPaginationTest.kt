/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.selectitem.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.account.fakes.FakeUserManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeGetSuggestedAutofillItems
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveIndexingStatus
import proton.android.pass.data.fakes.usecases.FakeObserveItemTypeCounts
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.fakes.usecases.FakeObserveItemsWithPasskeys
import proton.android.pass.data.fakes.usecases.FakeObservePagedItems
import proton.android.pass.data.fakes.usecases.FakeObservePinnedItems
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.domain.ShareId
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.searchoptions.fakes.FakeAutofillSearchOptionsRepository
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ShareTestFactory

internal class SelectItemViewModelPaginationTest {

    @get:Rule
    internal val dispatcher = MainDispatcherRule()

    private lateinit var instance: SelectItemViewModel
    private lateinit var accountManager: FakeAccountManager
    private lateinit var observeAutofillShares: FakeObserveAutofillShares
    private lateinit var observePagedItems: FakeObservePagedItems
    private lateinit var observeItemTypeCounts: FakeObserveItemTypeCounts
    private lateinit var observeIndexingStatus: FakeObserveIndexingStatus
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository

    @Before
    fun setup() {
        accountManager = FakeAccountManager()
        observeAutofillShares = FakeObserveAutofillShares()
        observePagedItems = FakeObservePagedItems()
        observeItemTypeCounts = FakeObserveItemTypeCounts()
        observeIndexingStatus = FakeObserveIndexingStatus()
        featureFlags = FakeFeatureFlagsPreferenceRepository()

        featureFlags.set(FeatureFlag.ENABLE_PAGINATION, true)
        accountManager.setAccounts(
            listOf(FakeAccountManager.createAccount(USER_A), FakeAccountManager.createAccount(USER_B))
        )
        observeAutofillShares.setValues(
            mapOf(
                USER_A to listOf(ShareTestFactory.Vault.create(id = SHARE_A.id, userId = USER_A.id)),
                USER_B to listOf(ShareTestFactory.Vault.create(id = SHARE_B.id, userId = USER_B.id))
            )
        )
        observeIndexingStatus.emitReady()
        observePagedItems.emitEmpty()
        observeItemTypeCounts.emitEmpty()

        instance = SelectItemViewModel(
            snackbarDispatcher = FakeSnackbarDispatcher(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            getSuggestedAutofillItems = FakeGetSuggestedAutofillItems(),
            observeItemsWithPasskeys = FakeObserveItemsWithPasskeys(),
            clock = FixedClock(),
            accountManager = accountManager,
            userManager = FakeUserManager(),
            preferenceRepository = FakePreferenceRepository(),
            observeItems = FakeObserveItems(),
            observePinnedItems = FakeObservePinnedItems(),
            autofillSearchOptionsRepository = FakeAutofillSearchOptionsRepository(),
            observeAutofillShares = observeAutofillShares,
            observeUpgradeInfo = FakeObserveUpgradeInfo(),
            getUserPlan = FakeGetUserPlan(),
            observePagedItems = observePagedItems,
            observeItemTypeCounts = observeItemTypeCounts,
            observeIndexingStatus = observeIndexingStatus,
            featureFlagsPreferencesRepository = featureFlags,
            appDispatchers = FakeAppDispatchers()
        )
        instance.setInitialState(SelectItemState.Autofill.CreditCard(title = "title"))
    }

    @Test
    fun `no selected account queries paged items of every account`() = runTest {
        awaitPagingAndCount()

        assertThat(observePagedItems.lastUserIds).containsExactly(USER_A, USER_B)
        assertThat(observePagedItems.lastShareIds).containsExactly(SHARE_A, SHARE_B)
        assertThat(observeItemTypeCounts.lastUserIds).containsExactly(USER_A, USER_B)
        assertThat(observeItemTypeCounts.lastShareIds).containsExactly(SHARE_A, SHARE_B)
    }

    @Test
    fun `selected account scopes paged items to that account`() = runTest {
        instance.onAccountSwitch(Some(USER_B))

        awaitPagingAndCount()

        assertThat(observePagedItems.lastUserIds).containsExactly(USER_B)
        assertThat(observePagedItems.lastShareIds).containsExactly(SHARE_B)
        assertThat(observeItemTypeCounts.lastUserIds).containsExactly(USER_B)
        assertThat(observeItemTypeCounts.lastShareIds).containsExactly(SHARE_B)
    }

    @Test
    fun `clearing the selected account queries every account again`() = runTest {
        instance.onAccountSwitch(Some(USER_A))
        instance.onAccountSwitch(None)

        awaitPagingAndCount()

        assertThat(observePagedItems.lastUserIds).containsExactly(USER_A, USER_B)
        assertThat(observeItemTypeCounts.lastUserIds).containsExactly(USER_A, USER_B)
    }

    private suspend fun awaitPagingAndCount() {
        instance.selectItemPagingFlow.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        instance.uiState.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        private val USER_A = UserId("user-a")
        private val USER_B = UserId("user-b")
        private val SHARE_A = ShareId("share-a")
        private val SHARE_B = ShareId("share-b")
    }
}
