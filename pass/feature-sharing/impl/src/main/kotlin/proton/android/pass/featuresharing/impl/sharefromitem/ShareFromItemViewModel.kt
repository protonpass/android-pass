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

package proton.android.pass.featuresharing.impl.sharefromitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class ShareFromItemViewModel @Inject constructor(
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    savedState: SavedStateHandleProvider,
    observeVaults: ObserveVaults,
    getVaultWithItemCount: GetVaultWithItemCountById,
    canCreateVault: CanCreateVault,
    getUserPlan: GetUserPlan
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedState.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedState.get().require(CommonNavArgId.ItemId.key))

    private val navEventState: MutableStateFlow<ShareFromItemNavEvent> =
        MutableStateFlow(ShareFromItemNavEvent.Unknown)

    private val canMoveToSharedVaultFlow: Flow<LoadingResult<Boolean>> = observeVaults()
        .map { vaults ->
            vaults.any { vault ->
                vault.shareId != shareId && vault.shared && vault.role.toPermissions().canCreate()
            }
        }
        .asLoadingResult()

    private val showCreateVaultFlow: Flow<LoadingResult<CreateNewVaultState>> = combine(
        canCreateVault(),
        getUserPlan()
    ) { canCreateVault, userPlan ->
        if (canCreateVault) {
            CreateNewVaultState.Allow
        } else {
            when (userPlan.planType) {
                is PlanType.Free -> CreateNewVaultState.Upgrade
                is PlanType.Paid, is PlanType.Trial -> CreateNewVaultState.VaultLimitReached
                is PlanType.Unknown -> CreateNewVaultState.Hide
            }
        }
    }.asLoadingResult()

    val state: StateFlow<ShareFromItemUiState> = combine(
        getVaultWithItemCount(shareId = shareId),
        canMoveToSharedVaultFlow,
        showCreateVaultFlow,
        navEventState
    ) { vault, canMoveToSharedVault, createVault, event ->
        val showCreateVault = createVault.getOrNull() ?: CreateNewVaultState.Hide

        ShareFromItemUiState(
            vault = vault.some(),
            itemId = itemId,
            showMoveToSharedVault = canMoveToSharedVault.getOrNull() ?: false,
            showCreateVault = showCreateVault,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ShareFromItemUiState.Initial(itemId)
    )

    fun moveItemToSharedVault() = viewModelScope.launch {
        bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))
        navEventState.update { ShareFromItemNavEvent.MoveToSharedVault }
    }

    fun clearEvent() = viewModelScope.launch {
        navEventState.update { ShareFromItemNavEvent.Unknown }
    }
}
