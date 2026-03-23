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

package proton.android.pass.features.sharing.sharingpermissions.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.api.repositories.GroupTarget
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.fakes.repositories.FakeBulkInviteRepository
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ShareRole
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.features.sharing.sharingpermissions.SharingType
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.test.MainDispatcherRule

class SharingPermissionsBottomSheetViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var bulkInviteRepository: FakeBulkInviteRepository
    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider

    @Before
    fun setUp() {
        bulkInviteRepository = FakeBulkInviteRepository()
        savedStateHandleProvider = FakeSavedStateHandleProvider()
    }

    @Test
    fun `remove user removes matching group target`() = runTest {
        val groupTarget = GroupTarget(
            groupId = GroupId("group-id"),
            name = "Security team",
            memberCount = 2,
            email = "security-team@proton.test",
            shareRole = ShareRole.Admin
        )
        val userTarget = UserTarget(
            email = "user@proton.test",
            shareRole = ShareRole.Read
        )
        bulkInviteRepository.storeInvites(listOf(userTarget, groupTarget))
        savedStateHandleProvider.get().apply {
            this[EditPermissionsModeNavArgId.key] = EditPermissionsMode.SingleUser.name
            this[EmailNavArgId.key] = groupTarget.email
            this[PermissionNavArgId.key] = SharingType.Admin.name
            this[CommonOptionalNavArgId.GroupId.key] = groupTarget.groupId.id
        }
        val viewModel = SharingPermissionsBottomSheetViewModel(
            bulkInviteRepository = bulkInviteRepository,
            savedStateHandleProvider = savedStateHandleProvider,
            featureFlagsPreferencesRepository = FakeFeatureFlagsPreferenceRepository()
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.displayRemove).isTrue()
            assertThat(state.mode).isEqualTo(
                SharingPermissionsEditMode.EditOne(
                    displayName = groupTarget.name,
                    sharingType = SharingType.Admin,
                    isGroup = true
                )
            )
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onDeleteUser()

        viewModel.state.test {
            assertThat(awaitItem().event).isEqualTo(SharingPermissionsBottomSheetEvent.Close)
        }
        assertThat(bulkInviteRepository.observeInvites().first()).containsExactly(userTarget)
    }
}
