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

package proton.android.pass.features.vault.bottomsheet.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class SelectVaultViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val canCreateItemInVault: CanCreateItemInVault,
    private val setDefaultVault: SetDefaultVault,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val selected: ShareId = ShareId(savedStateHandle.get().require(SelectedVaultArg.key))

    val state: StateFlow<SelectVaultUiState> = combine(
        observeVaultsWithItemCount(includeHidden = true).asLoadingResult(),
        observeUpgradeInfo().asLoadingResult()
    ) { vaultsResult, upgradeResult ->
        when (vaultsResult) {
            LoadingResult.Loading -> SelectVaultUiState.Loading
            is LoadingResult.Success -> successState(
                vaults = vaultsResult.data,
                upgradeResult = upgradeResult
            )

            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error observing vaults")
                PassLogger.w(TAG, vaultsResult.exception)
                snackbarDispatcher(VaultSnackbarMessage.CannotGetVaultListError)
                SelectVaultUiState.Error
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectVaultUiState.Uninitialised
    )

    private suspend fun successState(
        vaults: List<VaultWithItemCount>,
        upgradeResult: LoadingResult<UpgradeInfo>
    ): SelectVaultUiState {
        val showUpgradeMessage = upgradeResult.getOrNull()?.isUpgradeAvailable ?: false

        val shares = vaults.map { it.vault.shareId }
        return if (shares.contains(selected)) {
            val selectedVault = vaults.first { it.vault.shareId == selected }
            val vaultsList = vaults.map { vault ->
                val status = if (canCreateItemInVault(vault.vault)) {
                    VaultStatus.Selectable
                } else {
                    if (vault.vault.isOwned) {
                        VaultStatus.Disabled(VaultStatus.Reason.Downgraded)
                    } else {
                        VaultStatus.Disabled(VaultStatus.Reason.ReadOnly)
                    }
                }

                VaultWithStatus(
                    vault = vault,
                    status = status
                )
            }

            SelectVaultUiState.Success(
                vaults = vaultsList.toImmutableList(),
                selected = selectedVault,
                showUpgradeMessage = showUpgradeMessage
            )
        } else {
            PassLogger.w(TAG, "Error finding current vault")
            snackbarDispatcher(VaultSnackbarMessage.CannotFindVaultError)
            SelectVaultUiState.Error
        }
    }

    fun setLastUsedVault(shareId: ShareId) {
        viewModelScope.launch {
            runCatching { setDefaultVault(shareId) }
                .onSuccess {
                    PassLogger.d(TAG, "Last used vault set to $shareId")
                }
                .onFailure {
                    PassLogger.w(TAG, "Error setting last used vault")
                    PassLogger.w(TAG, it)
                }
        }
    }

    companion object {
        private const val TAG = "SelectVaultViewModel"
    }
}
