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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ObserveHasConfirmedInvite
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class RouterViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    itemSyncStatusRepository: ItemSyncStatusRepository,
    observeHasConfirmedInvite: ObserveHasConfirmedInvite
) : ViewModel() {

    private val routerEventFlow: MutableStateFlow<RouterEvent> = MutableStateFlow(RouterEvent.None)

    private val confirmedInviteFlow: Flow<Boolean> = observeHasConfirmedInvite()
        .map { hasConfirmedInvite ->
            if (hasConfirmedInvite) {
                observeHasConfirmedInvite.clear()
            }
            hasConfirmedInvite
        }
        .distinctUntilChanged()


    private val showSyncDialogFlow: Flow<SyncState> = combine(
        itemSyncStatusRepository.observeSyncStatus(),
        itemSyncStatusRepository.observeMode(),
        ::SyncState
    )

    init {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.getHasCompletedOnBoarding(),
                showSyncDialogFlow,
                confirmedInviteFlow,
                ::routerEvent
            ).collect { event ->
                routerEventFlow.update { event }
            }
        }
    }

    val eventStateFlow: StateFlow<RouterEvent> = routerEventFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RouterEvent.None
        )

    fun clearEvent() = viewModelScope.launch {
        routerEventFlow.update {
            RouterEvent.None
        }
    }

    private fun routerEvent(
        hasCompletedOnBoarding: HasCompletedOnBoarding,
        syncState: SyncState,
        hasConfirmedInvite: Boolean
    ) = when {
        hasConfirmedInvite -> RouterEvent.ConfirmedInvite
        hasCompletedOnBoarding == HasCompletedOnBoarding.NotCompleted -> RouterEvent.OnBoarding
        syncState.syncStatus.isSyncing() && syncState.syncMode == SyncMode.ShownToUser -> RouterEvent.SyncDialog
        else -> RouterEvent.None
    }

    private fun ItemSyncStatus.isSyncing() = this is ItemSyncStatus.Syncing || this is ItemSyncStatus.Started

    private data class SyncState(
        val syncStatus: ItemSyncStatus,
        val syncMode: SyncMode
    )
}

sealed interface RouterEvent {
    data object OnBoarding : RouterEvent
    data object SyncDialog : RouterEvent
    data object ConfirmedInvite : RouterEvent
    data object None : RouterEvent
}
