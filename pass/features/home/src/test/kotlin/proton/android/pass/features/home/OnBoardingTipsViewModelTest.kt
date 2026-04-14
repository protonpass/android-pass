/*
 * Copyright (c) 2023-2026 Proton AG
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.some
import proton.android.pass.data.fakes.repositories.FakeGroupRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveInvites
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsUiState
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.PendingInviteTestFactory

class OnBoardingTipsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingTipsViewModel
    private lateinit var observeInvites: FakeObserveInvites

    @Before
    fun setUp() {
        observeInvites = FakeObserveInvites()
        viewModel = OnBoardingTipsViewModel(
            observeInvites = observeInvites,
            observeCurrentUser = FakeObserveCurrentUser(),
            groupRepository = FakeGroupRepository()
        )
    }

    @Test
    fun `Should not show tip when there are no invites`() = runTest {
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should show invite tip when there is a pending invite`() = runTest {
        val pendingInvite = PendingInviteTestFactory.Vault.create()
        observeInvites.emitInvites(listOf(pendingInvite))

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Invite(pendingInvite).some()))
        }
    }
}
