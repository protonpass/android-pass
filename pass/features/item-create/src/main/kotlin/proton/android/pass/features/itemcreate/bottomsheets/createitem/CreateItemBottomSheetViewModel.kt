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

package proton.android.pass.features.itemcreate.bottomsheets.createitem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.items.ObserveCanCreateItems
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@HiltViewModel
class CreateItemBottomSheetViewModel @Inject constructor(
    homeSearchOptionsRepository: HomeSearchOptionsRepository,
    observeUpgradeInfo: ObserveUpgradeInfo,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeCanCreateItems: ObserveCanCreateItems
) : ViewModel() {

    private val navShareIdFlow: Flow<Option<ShareId>> =
        savedStateHandleProvider.get()
            .getStateFlow<String?>(CommonOptionalNavArgId.ShareId.key, null)
            .map { value -> value.toOption().map(::ShareId) }
    private val createItemModeFlow: Flow<CreateItemBottomSheetMode?> =
        savedStateHandleProvider.get().getStateFlow(CreateItemBottomSheetModeNavArgId.key, null)

    private val selectedShareIdFlow = combine(
        navShareIdFlow,
        homeSearchOptionsRepository.observeVaultSelectionOption().take(1),
        createItemModeFlow
    ) { navShareId: Option<ShareId>, vaultSelectionOption: VaultSelectionOption, mode: CreateItemBottomSheetMode? ->
        when {
            navShareId is Some -> navShareId.value
            mode == CreateItemBottomSheetMode.HomeFull &&
                vaultSelectionOption is VaultSelectionOption.Vault -> vaultSelectionOption.shareId

            else -> null
        }
    }

    internal val stateFlow: StateFlow<CreateItemBottomSheetUIState> = combine(
        observeUpgradeInfo(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.CUSTOM_TYPE_V1),
        selectedShareIdFlow,
        createItemModeFlow,
        observeCanCreateItems()
    ) { upgradeInfo, isCustomEnabled, selectedShare, mode, canCreateItems ->
        CreateItemBottomSheetUIState(
            shareId = selectedShare,
            mode = mode,
            createItemAliasUIState = CreateItemAliasUIState(
                canUpgrade = upgradeInfo.isUpgradeAvailable,
                aliasCount = upgradeInfo.totalAlias,
                aliasLimit = upgradeInfo.plan.aliasLimit.limitOrNull() ?: 0
            ),
            canCreateItems = canCreateItems,
            canCreateCustom = isCustomEnabled
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CreateItemBottomSheetUIState.Initial
        )

}

internal data class CreateItemBottomSheetUIState(
    val shareId: ShareId?,
    val mode: CreateItemBottomSheetMode?,
    val createItemAliasUIState: CreateItemAliasUIState,
    val canCreateItems: Boolean,
    val canCreateCustom: Boolean
) {

    internal companion object {

        val Initial = CreateItemBottomSheetUIState(
            shareId = null,
            mode = null,
            createItemAliasUIState = CreateItemAliasUIState.Initial,
            canCreateItems = false,
            canCreateCustom = false
        )

    }

}

internal data class CreateItemAliasUIState(
    val canUpgrade: Boolean,
    val aliasCount: Int,
    val aliasLimit: Int
) {

    internal companion object {

        val Initial = CreateItemAliasUIState(
            canUpgrade = false,
            aliasCount = 0,
            aliasLimit = 0
        )

    }

}
