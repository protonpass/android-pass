/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.home.drawer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanOrganiseVaults
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@HiltViewModel
class HomeDrawerViewModel @Inject constructor(
    canCreateVault: CanCreateVault,
    canOrganiseVaults: CanOrganiseVaults,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeItemCount: ObserveItemCount,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository,
    observeUpgradeInfo: ObserveUpgradeInfo,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeFolders: ObserveFolders
) : ViewModel() {
    private data class VaultShareKey(
        val userId: UserId,
        val shareId: ShareId
    )

    private data class FolderFlowInput(
        val isFoldersEnabled: Boolean,
        val shareKeys: List<VaultShareKey>
    )

    private data class VaultsWithFolders(
        val vaultShares: List<VaultWithItemCount>,
        val vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>
    )

    private val foldersEnabledFlow: Flow<Boolean> = featureFlagsPreferencesRepository[FeatureFlag.PASS_FOLDERS]

    private val vaultSharesItemsCountFlow: Flow<List<VaultWithItemCount>> =
        observeVaultsWithItemCount(includeHidden = false)
            .map { list -> list.sortedBy { it.vault.name.lowercase() } }

    private val itemCountSummaryOptionFlow: Flow<Some<ItemCountSummary>> =
        observeItemCount(
            applyItemStateToSharedItems = false,
            shareSelection = ShareSelection.AllShares,
            includeHiddenVault = false
        ).mapLatest(::Some)

    private val vaultShareKeysFlow: Flow<List<VaultShareKey>> = vaultSharesItemsCountFlow
        .map(::toVaultShareKeys)
        .distinctUntilChanged()

    private val folderFlowInput: Flow<FolderFlowInput> = combine(
        foldersEnabledFlow,
        vaultShareKeysFlow,
        ::FolderFlowInput
    )

    private val vaultFoldersFlow: Flow<Map<ShareId, PersistentList<FolderUiModel>>> =
        folderFlowInput.flatMapLatest(::observeVaultFolders)

    private val vaultsWithFoldersFlow: Flow<VaultsWithFolders> = combine(
        vaultSharesItemsCountFlow,
        vaultFoldersFlow,
        ::VaultsWithFolders
    )

    internal val stateFlow: StateFlow<HomeDrawerState> = combineN(
        foldersEnabledFlow,
        vaultsWithFoldersFlow,
        canCreateVault(),
        canOrganiseVaults(),
        homeSearchOptionsRepository.observeVaultSelectionOption(),
        itemCountSummaryOptionFlow,
        observeUpgradeInfo().asLoadingResult()
    ) { isFoldersEnabled,
        vaultsWithFolders,
        canCreateVault,
        canOrganiseVaults,
        vaultSelectionOption,
        itemCountSummaryOption,
        upgradeInfo ->
        buildHomeDrawerState(
            isFoldersEnabled = isFoldersEnabled,
            vaultsWithFolders = vaultsWithFolders,
            canCreateVault = canCreateVault,
            canOrganiseVaults = canOrganiseVaults,
            vaultSelectionOption = vaultSelectionOption,
            itemCountSummaryOption = itemCountSummaryOption,
            isUpgradeAvailable = upgradeInfo.getOrNull()?.isUpgradeAvailable ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = HomeDrawerState.Initial
    )

    internal fun setVaultSelection(vaultSelectionOption: VaultSelectionOption) {
        viewModelScope.launch {
            homeSearchOptionsRepository.setVaultSelectionOption(vaultSelectionOption)
        }
    }

    private fun toVaultShareKeys(vaults: List<VaultWithItemCount>): List<VaultShareKey> = vaults.map { vault ->
        VaultShareKey(
            userId = vault.vault.userId,
            shareId = vault.vault.shareId
        )
    }

    @Suppress("LongParameterList")
    private fun buildHomeDrawerState(
        isFoldersEnabled: Boolean,
        vaultsWithFolders: VaultsWithFolders,
        canCreateVault: Boolean,
        canOrganiseVaults: Boolean,
        vaultSelectionOption: VaultSelectionOption,
        itemCountSummaryOption: Some<ItemCountSummary>,
        isUpgradeAvailable: Boolean
    ): HomeDrawerState = HomeDrawerState(
        vaultShares = vaultsWithFolders.vaultShares,
        vaultFolders = vaultsWithFolders.vaultFolders,
        canCreateVault = canCreateVault,
        canOrganiseVaults = canOrganiseVaults,
        vaultSelectionOption = vaultSelectionOption,
        itemCountSummaryOption = itemCountSummaryOption,
        isUpgradeAvailable = isUpgradeAvailable,
        foldersEnabled = isFoldersEnabled
    )

    private fun observeVaultFolders(input: FolderFlowInput): Flow<Map<ShareId, PersistentList<FolderUiModel>>> {
        if (!input.isFoldersEnabled || input.shareKeys.isEmpty()) {
            return flowOf(emptyMap())
        }

        val folderFlows = input.shareKeys.map(::observeFolderTreeForShare)
        return combine(folderFlows) { shareFolderPairs -> shareFolderPairs.toMap() }
    }

    private fun observeFolderTreeForShare(shareKey: VaultShareKey): Flow<Pair<ShareId, PersistentList<FolderUiModel>>> =
        observeFolders(shareKey.userId, shareKey.shareId).map { folderList ->
            shareKey.shareId to FolderTreeBuilder.build(folderList)
        }
}
