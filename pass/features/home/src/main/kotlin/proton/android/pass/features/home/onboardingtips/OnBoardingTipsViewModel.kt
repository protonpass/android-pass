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

package proton.android.pass.features.home.onboardingtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveInvites
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.PendingGroupInvite
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.PendingUserInvite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    observeInvites: ObserveInvites,
    observeCurrentUser: ObserveCurrentUser,
    groupRepository: GroupRepository
) : ViewModel() {

    private val eventFlow: MutableStateFlow<OnBoardingTipsEvent> =
        MutableStateFlow(OnBoardingTipsEvent.Unknown)

    private val pendingInviteDataFlow: Flow<Pair<PendingInvite, String?>?> =
        observeInvites()
            .map { it.firstOrNull() }
            .distinctUntilChanged()
            .flatMapLatest { invite ->
                when (invite) {
                    null -> flowOf(null)
                    is PendingGroupInvite -> observeCurrentUser().map { user ->
                        val groupName = safeRunCatching {
                            groupRepository.retrieveGroup(
                                userId = user.userId,
                                groupId = GroupId(invite.invitedGroupId)
                            )?.name
                        }.onFailure { PassLogger.w(TAG, it) }
                            .getOrNull()
                            ?: invite.invitedEmail
                        invite to groupName
                    }
                    is PendingUserInvite -> flowOf(invite to null)
                }
            }

    internal val stateFlow: StateFlow<OnBoardingTipsUiState> = combine(
        pendingInviteDataFlow.map { data -> data?.let { Invite(it.first, it.second) }.toOption() },
        eventFlow,
        ::OnBoardingTipsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnBoardingTipsUiState()
    )

    internal fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            is Invite -> eventFlow.update {
                when (onBoardingTipPage.pendingInvite) {
                    is PendingInvite.GroupItem,
                    is PendingInvite.GroupVault ->
                        OnBoardingTipsEvent.OpenGroupInviteScreen(onBoardingTipPage.pendingInvite.inviteId)

                    is PendingInvite.UserItem,
                    is PendingInvite.UserVault ->
                        OnBoardingTipsEvent.OpenUserInviteScreen(onBoardingTipPage.pendingInvite.inviteToken)
                }
            }
        }
    }

    internal fun onDismiss(@Suppress("UNUSED_PARAMETER") onBoardingTipPage: OnBoardingTipPage) {
        // Invites cannot be dismissed
    }

    internal fun clearEvent() {
        eventFlow.update { OnBoardingTipsEvent.Unknown }
    }

    private companion object {
        private const val TAG = "OnBoardingTipsViewModel"
    }
}
