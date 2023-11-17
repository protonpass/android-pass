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

package proton.android.pass.featuremigrate.impl.selectvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.featuremigrate.impl.MigrateSnackbarMessage.CouldNotInit
import proton.android.pass.featuremigrate.impl.MigrateVaultFilter
import proton.android.pass.featuremigrate.impl.MigrateVaultFilterArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    observeVaults: ObserveVaultsWithItemCount,
    snackbarDispatcher: SnackbarDispatcher,
    private val savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val mode: Mode = getMode()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    val state: StateFlow<MigrateSelectVaultUiState> = combine(
        observeVaults().asLoadingResult(),
        eventFlow
    ) { vaultResult, event ->
        when (vaultResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Loading
            is LoadingResult.Error -> {
                snackbarDispatcher(CouldNotInit)
                PassLogger.w(TAG, vaultResult.exception, "Error observing active vaults")
                MigrateSelectVaultUiState.Error
            }

            is LoadingResult.Success -> MigrateSelectVaultUiState.Success(
                vaultList = vaultResult.data
                    .filter {
                        if (mode is Mode.MigrateItem && mode.filter == MigrateVaultFilter.Shared) {
                            it.vault.shared
                        } else {
                            true
                        }
                    }
                    .map {
                        val canCreate = it.vault.role.toPermissions().canCreate()
                        val isNotCurrentOne = it.vault.shareId != mode.shareId

                        VaultEnabledPair(
                            vault = it,
                            isEnabled = canCreate && isNotCurrentOne
                        )
                    }
                    .toImmutableList(),
                event = event,
                mode = mode.migrateMode()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Uninitialised
    )

    fun onVaultSelected(shareId: ShareId) {
        val event = when (mode) {
            is Mode.MigrateItem -> SelectVaultEvent.VaultSelectedForMigrateItem(
                sourceShareId = mode.shareId,
                itemId = mode.itemId,
                destinationShareId = shareId
            )

            is Mode.MigrateAllItems -> SelectVaultEvent.VaultSelectedForMigrateAll(
                sourceShareId = mode.shareId,
                destinationShareId = shareId
            )
        }

        eventFlow.update { event.toOption() }
    }

    fun clearEvent() {
        eventFlow.update { None }
    }

    private fun getMode(): Mode {
        val savedState = savedStateHandle.get()
        val sourceShareId = ShareId(savedState.require(CommonNavArgId.ShareId.key))
        return when (MigrateModeValue.valueOf(savedState.require(MigrateModeArg.key))) {
            MigrateModeValue.SingleItem -> Mode.MigrateItem(
                shareId = sourceShareId,
                itemId = ItemId(savedState.require(CommonOptionalNavArgId.ItemId.key)),
                filter = MigrateVaultFilter.valueOf(savedState.require(MigrateVaultFilterArg.key))
            )

            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(sourceShareId)
        }
    }

    internal sealed interface Mode {

        val shareId: ShareId

        data class MigrateItem(
            override val shareId: ShareId,
            val itemId: ItemId,
            val filter: MigrateVaultFilter
        ) : Mode

        data class MigrateAllItems(override val shareId: ShareId) : Mode

        fun migrateMode(): MigrateMode = when (this) {
            is MigrateItem -> MigrateMode.MigrateItem
            is MigrateAllItems -> MigrateMode.MigrateAll
        }
    }

    companion object {
        private const val TAG = "MigrateSelectVaultViewModel"
    }
}
