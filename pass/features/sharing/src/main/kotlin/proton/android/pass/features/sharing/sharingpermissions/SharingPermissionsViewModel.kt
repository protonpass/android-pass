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

package proton.android.pass.features.sharing.sharingpermissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.common.toUiState
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SharingPermissionsViewModel @Inject constructor(
    bulkInviteRepository: BulkInviteRepository,
    getVaultByShareId: GetVaultByShareId,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemIdOption: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        .toOption()
        .map(::ItemId)

    private val eventState: MutableStateFlow<SharingPermissionsEvents> =
        MutableStateFlow(SharingPermissionsEvents.Idle)

    internal val stateFlow: StateFlow<SharingPermissionsUIState> = combine(
        bulkInviteRepository.observeInvites().map { invites ->
            invites.map { it.toUiState() }
        },
        getVaultByShareId(shareId = shareId).asLoadingResult(),
        eventState,
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.RENAME_ADMIN_TO_MANAGER)
    ) { inviteTargets, vault, event, isRenameAdminToManagerEnabled ->
        val uiEvent = if (event == SharingPermissionsEvents.Idle && inviteTargets.isEmpty()) {
            SharingPermissionsEvents.BackToHome
        } else {
            event
        }
        SharingPermissionsUIState(
            itemIdOption = itemIdOption,
            inviteTargets = inviteTargets.toImmutableList(),
            vaultName = vault.getOrNull()?.name,
            isRenameAdminToManagerEnabled = isRenameAdminToManagerEnabled,
            event = uiEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SharingPermissionsUIState()
    )

    internal fun onPermissionsSubmit() {
        eventState.update {
            SharingPermissionsEvents.NavigateToSummary(shareId, itemIdOption)
        }
    }

    internal fun onConsumeEvent(event: SharingPermissionsEvents) {
        eventState.compareAndSet(event, SharingPermissionsEvents.Idle)
    }

}
