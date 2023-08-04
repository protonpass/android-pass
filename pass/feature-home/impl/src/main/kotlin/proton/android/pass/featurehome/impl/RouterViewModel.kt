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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class RouterViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    itemSyncStatusRepository: ItemSyncStatusRepository
) : ViewModel() {

    val eventStateFlow: StateFlow<RouterEvent> = combine(
        userPreferencesRepository.getHasCompletedOnBoarding(),
        itemSyncStatusRepository.observeSyncStatus(),
        ::routerEvent
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            val hasCompletedOnBoarding =
                userPreferencesRepository.getHasCompletedOnBoarding().first()
            val syncStatus = itemSyncStatusRepository.observeSyncStatus().first()
            routerEvent(hasCompletedOnBoarding, syncStatus)
        }
    )

    private fun routerEvent(
        hasCompletedOnBoarding: HasCompletedOnBoarding,
        syncStatus: ItemSyncStatus
    ) = when {
        hasCompletedOnBoarding == HasCompletedOnBoarding.NotCompleted -> RouterEvent.OnBoarding
        syncStatus.isSyncing() -> RouterEvent.SyncDialog
        else -> RouterEvent.None
    }

    private fun ItemSyncStatus.isSyncing() =
        this is ItemSyncStatus.Syncing || this is ItemSyncStatus.Started
}

enum class RouterEvent {
    OnBoarding,
    SyncDialog,
    None
}
