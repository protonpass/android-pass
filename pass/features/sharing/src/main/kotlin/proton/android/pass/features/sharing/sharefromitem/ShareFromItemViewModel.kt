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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSharingPolicy
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class ShareFromItemViewModel @Inject constructor(
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    getUserPlan: GetUserPlan,
    getItemById: GetItemById,
    observeShare: ObserveShare,
    observeOrganizationSharingPolicy: ObserveOrganizationSharingPolicy
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val navEventState: MutableStateFlow<ShareFromItemNavEvent> =
        MutableStateFlow(ShareFromItemNavEvent.Unknown)

    private val canUsePaidFeaturesFlow = getUserPlan()
        .map { userPlan ->
            when (userPlan.planType) {
                is PlanType.Paid -> true
                is PlanType.Free,
                is PlanType.Unknown -> false
            }
        }

    internal val stateFlow: StateFlow<ShareFromItemUiState> = combine(
        navEventState,
        canUsePaidFeaturesFlow,
        oneShot { getItemById(shareId, itemId) },
        observeShare(shareId),
        observeOrganizationSharingPolicy()
    ) { event,
        canUsePaidFeatures,
        item,
        share,
        organizationSharingPolicy ->
        ShareFromItemUiState(
            shareId = shareId,
            itemId = itemId,
            event = event,
            canUsePaidFeatures = canUsePaidFeatures,
            itemOption = item.some(),
            shareOption = share.some(),
            organizationSharingPolicy = organizationSharingPolicy
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
