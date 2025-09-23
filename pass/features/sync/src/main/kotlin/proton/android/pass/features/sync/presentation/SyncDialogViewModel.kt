/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.runCatching
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class SyncDialogViewModel @Inject constructor(
    observeVaults: ObserveVaults,
    private val syncStatusRepository: ItemSyncStatusRepository,
    private val refreshContent: RefreshContent
) : ViewModel() {

    internal val state: StateFlow<SyncDialogState> = combine(
        syncStatusRepository.observeSyncStatus(),
        syncStatusRepository.observeDownloadedItemsStatus(),
        syncStatusRepository.observeInsertedItemsStatus(),
        observeVaults(includeHidden = true).asLoadingResult(),
        ::SyncDialogState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncDialogState.Initial
    )

    internal fun onDismissSync() = viewModelScope.launch {
        syncStatusRepository.setMode(mode = SyncMode.Background)
    }

    internal fun onRetrySync() = viewModelScope.launch {
        runCatching { refreshContent() }
            .onError { error ->
                PassLogger.w(TAG, "Error retrying items sync")
                PassLogger.w(TAG, error)
            }
    }

    private companion object {

        private const val TAG = "SyncDialogViewModel"

    }

}
