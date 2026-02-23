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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonpresentation.api.folders.FolderTreeBuilder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.data.api.usecases.folders.ObserveFolders
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SelectVaultViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val canCreateItemInVault: CanCreateItemInVault,
    private val setDefaultVault: SetDefaultVault,
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeFolders: ObserveFolders,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private data class VaultShareKey(
        val userId: UserId,
        val shareId: ShareId
    )

    private val selected: ShareId = ShareId(savedStateHandle.get().require(SelectedVaultArg.key))

    private val selectedFolderId: FolderId? =
        savedStateHandle.get().get<String?>(CommonOptionalNavArgId.FolderId.key)
            ?.let { FolderId(it) }

    private val foldersEnabledFlow: Flow<Boolean> =
        featureFlagsPreferencesRepository[FeatureFlag.PASS_FOLDERS]

    private val vaultsFlow = observeVaultsWithItemCount(includeHidden = true)

    private val vaultShareKeysFlow: Flow<List<VaultShareKey>> = vaultsFlow
        .map { vaults ->
            vaults.asSequence()
                .map { VaultShareKey(userId = it.vault.userId, shareId = it.vault.shareId) }
                .distinct()
                .sortedWith(
                    compareBy<VaultShareKey> { it.userId.id }.thenBy { it.shareId.id }
                )
                .toList()
        }
        .distinctUntilChanged()

    private val vaultFoldersFlow: Flow<Map<ShareId, PersistentList<FolderUiModel>>> =
        combine(foldersEnabledFlow, vaultShareKeysFlow) { enabled: Boolean, keys: List<VaultShareKey> ->
            enabled to keys
        }.flatMapLatest { (enabled, keys) ->
            if (!enabled || keys.isEmpty()) {
                return@flatMapLatest flowOf<Map<ShareId, PersistentList<FolderUiModel>>>(emptyMap())
            }
            val flows: List<Flow<Pair<ShareId, PersistentList<FolderUiModel>>>> = keys.map { key ->
                observeFolders(key.userId, key.shareId)
                    .distinctUntilChanged()
                    .map<_, Pair<ShareId, PersistentList<FolderUiModel>>> { folderList ->
                        key.shareId to FolderTreeBuilder.build(folderList)
                    }
                    .onStart { emit(key.shareId to FolderTreeBuilder.build(emptyList())) }
            }
            combine(flows) { pairs ->
                pairs.toMap()
            }.distinctUntilChanged()
        }

    val state: StateFlow<SelectVaultUiState> = combine(
        vaultsFlow.asLoadingResult(),
        observeUpgradeInfo().asLoadingResult(),
        foldersEnabledFlow,
        vaultFoldersFlow
    ) { vaultsResult, upgradeResult, foldersEnabled, vaultFolders ->
        when (vaultsResult) {
            LoadingResult.Loading -> SelectVaultUiState.Loading
            is LoadingResult.Success -> successState(
                vaults = vaultsResult.data,
                upgradeResult = upgradeResult,
                foldersEnabled = foldersEnabled,
                vaultFolders = vaultFolders
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
        upgradeResult: LoadingResult<UpgradeInfo>,
        foldersEnabled: Boolean,
        vaultFolders: Map<ShareId, PersistentList<FolderUiModel>>
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
                    vaultWithItemCount = vault,
                    status = status
                )
            }

            SelectVaultUiState.Success(
                vaults = vaultsList.toImmutableList(),
                selected = selectedVault,
                showUpgradeMessage = showUpgradeMessage,
                foldersEnabled = foldersEnabled,
                vaultFolders = vaultFolders.toImmutableMap(),
                selectedFolderId = selectedFolderId
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
