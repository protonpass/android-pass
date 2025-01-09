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

package proton.android.pass.features.sharing.sharefromitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.VaultAccessData
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class ShareFromItemViewModel @Inject constructor(
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeVaults: ObserveVaults,
    getVaultWithItemCount: GetVaultWithItemCountById,
    canCreateVault: CanCreateVault,
    getUserPlan: GetUserPlan,
    getItemById: GetItemById,
    canManageVaultAccess: CanManageVaultAccess,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    observeShare: ObserveShare
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

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

    private val canUsePaidFeaturesFlow = getUserPlan()
        .map { userPlan ->
            when (userPlan.planType) {
                is PlanType.Paid,
                is PlanType.Trial -> true

                is PlanType.Free,
                is PlanType.Unknown -> false
            }
        }

    private val shareFlow = oneShot { observeShare(shareId).first() }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    private val vaultWithItemCountOptionFlow: Flow<Option<VaultWithItemCount>> = shareFlow
        .flatMapLatest { share ->
            when (share) {
                is Share.Item -> flowOf(None)
                is Share.Vault -> getVaultWithItemCount(shareId = shareId).mapLatest(::Some)
            }
        }

    private val vaultAccessDataFlow = shareFlow
        .mapLatest { share ->
            when (val vaultOption = share.toVault()) {
                None -> VaultAccessData(canManageAccess = false, canViewMembers = false)
                is Some -> canManageVaultAccess(vaultOption.value)
            }
        }

    internal val stateFlow: StateFlow<ShareFromItemUiState> = combineN(
        vaultWithItemCountOptionFlow,
        canMoveToSharedVaultFlow,
        showCreateVaultFlow,
        navEventState,
        canUsePaidFeaturesFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1),
        oneShot { getItemById(shareId, itemId) },
        vaultAccessDataFlow
    ) { vaultWithItemCountOption, canMoveToSharedVault, createVault, event, canUsePaidFeatures,
        isItemSharingAvailable, item, vaultAccessData ->
        ShareFromItemUiState(
            shareId = shareId,
            itemId = itemId,
            vault = vaultWithItemCountOption,
            showMoveToSharedVault = canMoveToSharedVault.getOrNull() ?: false,
            showCreateVault = createVault.getOrNull() ?: CreateNewVaultState.Hide,
            event = event,
            canUsePaidFeatures = canUsePaidFeatures,
            vaultAccessData = vaultAccessData,
            isItemSharingAvailable = isItemSharingAvailable,
            itemOption = item.some()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ShareFromItemUiState.initial(shareId, itemId)
    )

    internal fun moveItemToSharedVault() {
        viewModelScope.launch {
            bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))
            navEventState.update { ShareFromItemNavEvent.MoveToSharedVault }
        }
    }

    internal fun onEventConsumed(event: ShareFromItemNavEvent) {
        navEventState.compareAndSet(event, ShareFromItemNavEvent.Unknown)
    }

}
