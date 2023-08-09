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

package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import proton.pass.domain.canCreate
import proton.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class SelectVaultViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandleProvider,
    canPerformPaidAction: CanPerformPaidAction
) : ViewModel() {

    private val selected: ShareId = ShareId(savedStateHandle.get().require(SelectedVaultArg.key))

    val state: StateFlow<SelectVaultUiState> = combine(
        observeVaultsWithItemCount().asLoadingResult(),
        observeUpgradeInfo().asLoadingResult(),
        canPerformPaidAction().asLoadingResult()
    ) { vaultsResult, upgradeResult, selectOtherVaultResult ->
        when (vaultsResult) {
            LoadingResult.Loading -> SelectVaultUiState.Loading
            is LoadingResult.Success -> successState(
                vaults = vaultsResult.data,
                selectOtherVaultResult = selectOtherVaultResult,
                upgradeResult = upgradeResult
            )

            is LoadingResult.Error -> {
                PassLogger.w(TAG, vaultsResult.exception, "Error observing vaults")
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
        selectOtherVaultResult: LoadingResult<Boolean>,
        upgradeResult: LoadingResult<UpgradeInfo>
    ): SelectVaultUiState {
        val canSelectOtherVault = selectOtherVaultResult.getOrNull() ?: false

        val showUpgradeMessage = if (canSelectOtherVault) {
            false
        } else {
            upgradeResult.getOrNull()?.isUpgradeAvailable ?: false
        }

        val shares = vaults.map { it.vault.shareId }
        return if (shares.contains(selected)) {
            val selectedVault = vaults.first { it.vault.shareId == selected }
            val vaultsList = vaults.map { vault ->
                val permissions = vault.vault.role.toPermissions()
                when {
                    vault.vault.isPrimary -> VaultWithStatus(
                        vault = vault,
                        status = VaultStatus.Selectable
                    )
                    !canSelectOtherVault -> VaultWithStatus(
                        vault = vault,
                        status = VaultStatus.Disabled(VaultStatus.Reason.Downgraded)
                    )
                    !permissions.canCreate() -> VaultWithStatus(
                        vault = vault,
                        status = VaultStatus.Disabled(VaultStatus.Reason.ReadOnly)
                    )
                    else -> VaultWithStatus(
                        vault = vault,
                        status = VaultStatus.Selectable
                    )
                }
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

    companion object {
        private const val TAG = "SelectVaultViewModel"
    }
}
