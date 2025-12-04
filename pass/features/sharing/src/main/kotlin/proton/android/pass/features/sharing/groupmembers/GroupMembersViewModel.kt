/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.sharing.groupmembers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveGroupMembersByGroup
import proton.android.pass.domain.GroupId
import proton.android.pass.features.sharing.GroupIdArgId
import javax.inject.Inject

@HiltViewModel
internal class GroupMembersViewModel @Inject constructor(
    observeGroupMembersByGroup: ObserveGroupMembersByGroup,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: GroupId = GroupId(savedStateHandle.require(GroupIdArgId.key))

    val uiState: StateFlow<GroupMembersUiState> = observeGroupMembersByGroup()
        .asLoadingResult()
        .map { result ->
            when (result) {
                is LoadingResult.Loading -> GroupMembersUiState.Loading
                is LoadingResult.Error -> GroupMembersUiState.Loading.copy(isError = true)
                is LoadingResult.Success -> {
                    val groupMembers = result.data.find { it.group.id == groupId }
                    GroupMembersUiState.Loading.copy(
                        groupName = groupMembers?.group?.name ?: "",
                        members = groupMembers?.members?.map { member ->
                            GroupMemberUiModel(email = member.email)
                        }
                            ?.toImmutableList()
                            ?: persistentListOf(),
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupMembersUiState.Loading
        )

}
