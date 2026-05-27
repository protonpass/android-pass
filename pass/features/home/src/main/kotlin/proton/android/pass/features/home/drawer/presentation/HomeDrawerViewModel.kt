/*
 * Copyright (c) 2024-2026 Proton AG
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
import kotlinx.coroutines.flow.onStart
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
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.capabilities.CanOrganiseVaults
import proton.android.pass.data.api.usecases.folders.FolderLimitsData
import proton.android.pass.data.api.usecases.folders.ObserveFolderLimits
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
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
    canCreateFolder: CanCreateFolder,
    canCreateVault: CanCreateVault,
    canOrganiseVaults: CanOrganiseVaults,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeItemCount: ObserveItemCount,
    private val homeSearchOptionsRepository: HomeSearchOptionsRepository,
    observeUpgradeInfo: ObserveUpgradeInfo,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeFolders: ObserveFoldersByParentId,
    observeFolderLimits: ObserveFolderLimits
) : ViewModel() {
    private data class VaultShareKey(
        val userId: UserId,
        val shareId: ShareId
    )

    private data class FolderFlowInput(
        val isFoldersEnabled: Boolean,
        val shareKeys: List<VaultShareKey>,
        val folderLimits: FolderLimitsData
    )

    private data class VaultsWithFolders(
        val vaultShares: List<VaultWithItemCount>,
        val vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>,
        val vaultFolderAtLimit: Set<ShareId>
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

    private val folderLimitsFlow: Flow<FolderLimitsData> = observeFolderLimits()

    private val folderFlowInput: Flow<FolderFlowInput> = combine(
        foldersEnabledFlow,
        vaultShareKeysFlow,
        folderLimitsFlow
    ) { isFoldersEnabled, shareKeys, folderLimits ->
        FolderFlowInput(isFoldersEnabled, shareKeys, folderLimits)
    }

    private val vaultFoldersFlow: Flow<Pair<Map<ShareId, PersistentList<FolderUiModel>>, Set<ShareId>>> =
        folderFlowInput.flatMapLatest(::observeVaultFolders)

    private data class FolderCapabilities(
        val canCreateShareIds: Set<ShareId>,
        val needsUpgradeShareIds: Set<ShareId>
    )

    private val folderCapabilitiesFlow: Flow<FolderCapabilities> = combine(
        vaultShareKeysFlow,
        observeUpgradeInfo().map { it.isUpgradeAvailable }
    ) { keys, isUpgradeAvailable ->
        keys to isUpgradeAvailable
    }.flatMapLatest { (keys, isUpgradeAvailable) ->
        if (keys.isEmpty()) return@flatMapLatest flowOf(FolderCapabilities(emptySet(), emptySet()))
        canCreateFolder(keys.map { it.shareId }).map { map ->
            FolderCapabilities(
                canCreateShareIds = map.filterValues { it.isAllowed }.keys,
                needsUpgradeShareIds = if (isUpgradeAvailable) {
                    map.filterValues { it.needsUpgrade }.keys
                } else {
                    emptySet()
                }
            )
        }
    }

    private val vaultsWithFoldersFlow: Flow<VaultsWithFolders> = combine(
        vaultSharesItemsCountFlow,
        vaultFoldersFlow
    ) { shares, (folders, atLimit) ->
        VaultsWithFolders(
            vaultShares = shares,
            vaultFolders = folders,
            vaultFolderAtLimit = atLimit
        )
    }

    internal val stateFlow: StateFlow<HomeDrawerState> = combineN(
        foldersEnabledFlow,
        vaultsWithFoldersFlow,
        folderCapabilitiesFlow,
        canCreateVault(),
        canOrganiseVaults(),
        homeSearchOptionsRepository.observeVaultSelectionOption(),
        itemCountSummaryOptionFlow,
        observeUpgradeInfo().asLoadingResult()
    ) { isFoldersEnabled,
        vaultsWithFolders,
        folderCapabilities,
        canCreateVault,
        canOrganiseVaults,
        vaultSelectionOption,
        itemCountSummaryOption,
        upgradeInfo ->
        buildHomeDrawerState(
            isFoldersEnabled = isFoldersEnabled,
            vaultsWithFolders = vaultsWithFolders,
            canCreateFolderShareIds = folderCapabilities.canCreateShareIds,
            canCreateFolderNeedsUpgradeShareIds = folderCapabilities.needsUpgradeShareIds,
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

    private fun toVaultShareKeys(vaults: List<VaultWithItemCount>): List<VaultShareKey> = vaults
        .asSequence()
        .map { vault ->
            VaultShareKey(
                userId = vault.vault.userId,
                shareId = vault.vault.shareId
            )
        }
        .distinct()
        .sortedWith(
            compareBy<VaultShareKey> { it.userId.id }
                .thenBy { it.shareId.id }
        )
        .toList()

    @Suppress("LongParameterList")
    private fun buildHomeDrawerState(
        isFoldersEnabled: Boolean,
        vaultsWithFolders: VaultsWithFolders,
        canCreateFolderShareIds: Set<ShareId>,
        canCreateFolderNeedsUpgradeShareIds: Set<ShareId>,
        canCreateVault: Boolean,
        canOrganiseVaults: Boolean,
        vaultSelectionOption: VaultSelectionOption,
        itemCountSummaryOption: Some<ItemCountSummary>,
        isUpgradeAvailable: Boolean
    ): HomeDrawerState = HomeDrawerState(
        vaultShares = vaultsWithFolders.vaultShares,
        vaultFolders = vaultsWithFolders.vaultFolders,
        vaultFolderAtLimit = vaultsWithFolders.vaultFolderAtLimit,
        canCreateFolderShareIds = canCreateFolderShareIds,
        canCreateFolderNeedsUpgradeShareIds = canCreateFolderNeedsUpgradeShareIds,
        canCreateVault = canCreateVault,
        canOrganiseVaults = canOrganiseVaults,
        vaultSelectionOption = vaultSelectionOption,
        itemCountSummaryOption = itemCountSummaryOption,
        needsToUpgrade = isUpgradeAvailable,
        foldersEnabled = isFoldersEnabled
    )

    private fun observeVaultFolders(
        input: FolderFlowInput
    ): Flow<Pair<Map<ShareId, PersistentList<FolderUiModel>>, Set<ShareId>>> {
        if (!input.isFoldersEnabled || input.shareKeys.isEmpty()) {
            return flowOf(emptyMap<ShareId, PersistentList<FolderUiModel>>() to emptySet())
        }

        val folderFlows = input.shareKeys.map { observeFolderTreeForShare(it, input.folderLimits) }
        return combine(folderFlows) { entries ->
            val folders = entries.associate { (shareId, tree, _) -> shareId to tree }
            val atLimit = entries
                .filter { (_, _, count) -> count >= input.folderLimits.maxCount }
                .mapTo(mutableSetOf()) { (shareId, _, _) -> shareId }
            folders to atLimit
        }.distinctUntilChanged()
    }

    private fun observeFolderTreeForShare(
        shareKey: VaultShareKey,
        limits: FolderLimitsData
    ): Flow<Triple<ShareId, PersistentList<FolderUiModel>, Int>> = observeFolders(shareKey.userId, shareKey.shareId)
        .distinctUntilChanged()
        .map { folderList ->
            Triple(shareKey.shareId, FolderTreeBuilder.build(folderList), folderList.size)
        }
        .onStart {
            emit(Triple(shareKey.shareId, FolderTreeBuilder.build(emptyList()), limits.maxCount))
        }

}
