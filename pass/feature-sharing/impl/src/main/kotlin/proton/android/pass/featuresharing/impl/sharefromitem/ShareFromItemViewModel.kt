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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.canCreate
import proton.pass.domain.toPermissions
import javax.inject.Inject

@HiltViewModel
class ShareFromItemViewModel @Inject constructor(
    savedState: SavedStateHandleProvider,
    observeVaults: ObserveVaults,
    getVaultWithItemCount: GetVaultWithItemCountById,
    canCreateVault: CanCreateVault
) : ViewModel() {

    private val shareId: ShareId = ShareId(savedState.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId = ItemId(savedState.get().require(CommonNavArgId.ItemId.key))

    private val canMoveToSharedVaultFlow: Flow<LoadingResult<Boolean>> = observeVaults()
        .map { vaults ->
            vaults.any { vault ->
                vault.shareId != shareId && vault.shared && vault.role.toPermissions().canCreate()
            }
        }
        .asLoadingResult()

    val state: StateFlow<ShareFromItemUiState> = combine(
        getVaultWithItemCount(shareId = shareId),
        canMoveToSharedVaultFlow,
        canCreateVault()
    ) { vault, canMoveToSharedVault, showCreateVault ->
        ShareFromItemUiState(
            vault = vault.some(),
            itemId = itemId,
            showMoveToSharedVault = canMoveToSharedVault.getOrNull() ?: false,
            showCreateVault = showCreateVault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ShareFromItemUiState.Initial(itemId)
    )

}
