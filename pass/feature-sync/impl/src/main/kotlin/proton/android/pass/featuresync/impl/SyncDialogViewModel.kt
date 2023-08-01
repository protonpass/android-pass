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

package proton.android.pass.featuresync.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltViewModel
class SyncDialogViewModel @Inject constructor(
    syncStatusRepository: ItemSyncStatusRepository,
    observeVaults: ObserveVaults
) : ViewModel() {

    private val observeFinishEvents: Flow<Boolean> = syncStatusRepository.observeSyncStatus()
        .map { it is ItemSyncStatus.CompletedSyncing }

    val state: StateFlow<SyncDialogUiState> = combine(
        syncStatusRepository.observeAccSyncStatus(),
        observeFinishEvents,
        observeVaults().asLoadingResult()
    ) { accSync, isFinished, vaultsResult ->
        val syncItemMap = (vaultsResult.getOrNull() ?: emptyList())
            .associateBy { it.shareId }
            .mapValues { (shareId, vault) ->
                val syncItem = accSync[shareId]
                SyncItem(
                    vault = vault,
                    current = syncItem?.current ?: -1,
                    total = syncItem?.total ?: -1
                )
            }
            .toPersistentMap()

        SyncDialogUiState(
            syncItemMap = syncItemMap,
            isFinished = isFinished
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncDialogUiState(persistentMapOf(), false)
    )
}

data class SyncDialogUiState(
    val syncItemMap: ImmutableMap<ShareId, SyncItem>,
    val isFinished: Boolean
)

data class SyncItem(
    val vault: Vault,
    val current: Int,
    val total: Int
)
