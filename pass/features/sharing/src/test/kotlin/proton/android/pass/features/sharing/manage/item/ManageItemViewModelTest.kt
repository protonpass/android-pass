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

package proton.android.pass.features.sharing.manage.item

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.usecases.FakeObserveGroupMembersByGroup
import proton.android.pass.data.fakes.usecases.organizations.FakeObserveOrganizationSharingPolicy
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShareItemMembers
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShareItemsCount
import proton.android.pass.data.fakes.usecases.shares.FakeObserveSharePendingInvites
import proton.android.pass.domain.organizations.OrganizationSharingPolicy
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemState
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemViewModel
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ShareTestFactory

internal class ManageItemViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var observeShare: FakeObserveShare
    private lateinit var observeShareItemMembers: FakeObserveShareItemMembers
    private lateinit var observeSharePendingInvites: FakeObserveSharePendingInvites
    private lateinit var observeShareItemsCount: FakeObserveShareItemsCount
    private lateinit var observeOrganizationSharingPolicy: FakeObserveOrganizationSharingPolicy
    private lateinit var observeGroupMembersByGroup: FakeObserveGroupMembersByGroup
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository

    @Before
    fun setup() {
        observeShare = FakeObserveShare()
        observeShareItemMembers = FakeObserveShareItemMembers()
        observeSharePendingInvites = FakeObserveSharePendingInvites()
        observeShareItemsCount = FakeObserveShareItemsCount()
        observeOrganizationSharingPolicy = FakeObserveOrganizationSharingPolicy()
        observeGroupMembersByGroup = FakeObserveGroupMembersByGroup()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
    }

    private fun createViewModel(): ManageItemViewModel = ManageItemViewModel(
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID
            get()[CommonNavArgId.ItemId.key] = ITEM_ID
        },
        observeShare = observeShare,
        observeShareItemMembers = observeShareItemMembers,
        observeSharePendingInvites = observeSharePendingInvites,
        observeShareItemsCount = observeShareItemsCount,
        observeOrganizationSharingPolicy = observeOrganizationSharingPolicy,
        observeGroupMembersByGroup = observeGroupMembersByGroup,
        snackbarDispatcher = FakeSnackbarDispatcher(),
        featureFlagsPreferencesRepository = featureFlags
    )

    private fun emitRequiredState() {
        observeShare.emitValue(ShareTestFactory.Item.create(id = SHARE_ID))
        observeShareItemMembers.emitValue(emptyList())
        observeSharePendingInvites.emitValue(emptyList())
        observeShareItemsCount.emitValue(0)
        observeOrganizationSharingPolicy.emitValue(OrganizationSharingPolicy.Default)
    }

    @Test
    fun `isRenameAdminToManagerEnabled is true when feature flag is enabled`() = runTest {
        featureFlags.set(FeatureFlag.RENAME_ADMIN_TO_MANAGER, true)
        val viewModel = createViewModel()
        emitRequiredState()

        viewModel.stateFlow.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ManageItemState.Success::class.java)
            assertThat((state as ManageItemState.Success).isRenameAdminToManagerEnabled).isTrue()
        }
    }

    @Test
    fun `isRenameAdminToManagerEnabled is false when feature flag is disabled`() = runTest {
        featureFlags.set(FeatureFlag.RENAME_ADMIN_TO_MANAGER, false)
        val viewModel = createViewModel()
        emitRequiredState()

        viewModel.stateFlow.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ManageItemState.Success::class.java)
            assertThat((state as ManageItemState.Success).isRenameAdminToManagerEnabled).isFalse()
        }
    }

    private companion object {
        const val SHARE_ID = "share-id"
        const val ITEM_ID = "item-id"
    }

}
