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

package proton.android.pass.features.migrate.warningshared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.data.api.usecases.items.GetMigrationItemsSelection
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.features.migrate.MigrateModeArg
import proton.android.pass.features.migrate.MigrateModeValue
import proton.android.pass.features.migrate.MigrateVaultFilter
import proton.android.pass.features.migrate.MigrateVaultFilterArg
import proton.android.pass.features.migrate.selectvault.MigrateSelectVaultViewModel.Mode
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class MigrateSharedWarningViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    observeEncryptedItems: ObserveEncryptedItems,
    getMigrationItemsSelection: GetMigrationItemsSelection
) : ViewModel() {

    private val migrateModeValue: MigrateModeValue = savedStateHandleProvider.get()
        .require<String>(MigrateModeArg.key)
        .let(MigrateModeValue::valueOf)

    private val mode: Mode = when (migrateModeValue) {
        MigrateModeValue.SelectedItems -> Mode.MigrateSelectedItems(
            filter = savedStateHandleProvider.get()
                .require<String>(MigrateVaultFilterArg.key)
                .let(MigrateVaultFilter::valueOf)
        )

        MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(
            shareId = savedStateHandleProvider.get()
                .require<String>(CommonNavArgId.ShareId.key)
                .let(::ShareId)
        )
    }

    private val eventFlow = MutableStateFlow<MigrateSharedWarningEvent>(
        value = MigrateSharedWarningEvent.Idle
    )

    private val isLoadingStateFlow = MutableStateFlow<IsLoadingState>(
        value = IsLoadingState.NotLoading
    )

    private val itemsMigrationSelectionFlow = when (mode) {
        is Mode.MigrateAllItems -> observeEncryptedItems(
            selection = ShareSelection.Share(mode.shareId),
            itemState = ItemState.Active,
            filter = ItemTypeFilter.All,
            includeHidden = false
        ).mapLatest { encryptedItems ->
            mapOf(mode.shareId to encryptedItems.map { it.id })
        }

        is Mode.MigrateSelectedItems -> bulkMoveToVaultRepository.observe()
            .mapLatest { selectedItemsOption ->
                selectedItemsOption.value().orEmpty()
            }
    }.mapLatest(getMigrationItemsSelection::invoke)

    internal val stateFlow: StateFlow<MigrateSharedWarningState> = combine(
        eventFlow,
        isLoadingStateFlow,
        itemsMigrationSelectionFlow,
        ::MigrateSharedWarningState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MigrateSharedWarningState.Initial
    )

    internal fun onConsumeEvent(event: MigrateSharedWarningEvent) {
        eventFlow.compareAndSet(event, MigrateSharedWarningEvent.Idle)
    }

    internal fun onMigrate() {
        when (mode) {
            is Mode.MigrateAllItems -> MigrateSharedWarningEvent.OnMigrateVault(
                shareId = mode.shareId
            )

            is Mode.MigrateSelectedItems -> MigrateSharedWarningEvent.OnMigrateItems(
                filter = mode.filter
            )
        }.also { event ->
            eventFlow.update { event }
        }
    }

}
