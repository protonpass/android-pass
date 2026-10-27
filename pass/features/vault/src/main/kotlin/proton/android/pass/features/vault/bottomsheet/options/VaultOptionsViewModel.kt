/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.data.api.usecases.ObservePrimaryUserId
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.attachments.SetVaultOfflineAttachments
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.capabilities.CanManageVaultAccess
import proton.android.pass.data.api.usecases.capabilities.CanMigrateVault
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.folders.ObserveFolderLimits
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.features.vault.VaultSnackbarMessage.CannotFindVaultError
import proton.android.pass.features.vault.VaultSnackbarMessage.CannotGetVaultListError
import proton.android.pass.features.vault.VaultSnackbarMessage.CannotMigrateVaultError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DownloadAllAttachmentsPreference
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class VaultOptionsViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    observeVaults: ObserveVaults,
    canShareShare: CanShareShare,
    canMigrateVault: CanMigrateVault,
    canManageVaultAccess: CanManageVaultAccess,
    canCreateFolder: CanCreateFolder,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandleProvider,
    private val observeEncryptedItems: ObserveEncryptedItems,
    preferencesRepository: FeatureFlagsPreferencesRepository,
    observeFoldersByParentId: ObserveFoldersByParentId,
    observeFolderLimits: ObserveFolderLimits,
    userPreferencesRepository: UserPreferencesRepository,
    private val setVaultOfflineAttachments: SetVaultOfflineAttachments,
    private val observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val navShareId: ShareId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val canShare: Flow<Boolean> = flow { emit(canShareShare(navShareId)) }
        .map { it.value }
        .distinctUntilChanged()

    private val eventFlow = MutableStateFlow<VaultOptionsEvent>(
        value = VaultOptionsEvent.Idle
    )

    private data class FolderCounts(val rootCount: Int, val totalCount: Int)

    private val folderCountsFlow = combine(
        observeFoldersByParentId(navShareId, null).map { it.size },
        observeFoldersByParentId(navShareId).map { it.size },
        ::FolderCounts
    )

    private val folderLimitsFlow = observeFolderLimits()

    internal val state: StateFlow<VaultOptionsUiState> = combineN(
        observeVaults(includeHidden = true).asLoadingResult(),
        canShare,
        eventFlow,
        canCreateFolder(navShareId),
        observeUpgradeInfo().asLoadingResult(),
        folderCountsFlow,
        folderLimitsFlow,
        observePrimaryUserId().flatMapLatest { userId ->
            if (userId == null) flowOf(DownloadAllAttachmentsPreference.Disabled)
            else userPreferencesRepository.observeDownloadAllAttachmentsPref(userId)
        },
        preferencesRepository.get<Boolean>(FeatureFlag.PASS_OFFLINE_ATTACHMENTS)
    ) { vaultResult, canShare, event, canCreateFolder,
        upgradeResult, folderCounts, folderLimits, downloadAllPref, offlineAttachmentsFF ->
        val rootFolderCount = folderCounts.rootCount
        val totalFolderCount = folderCounts.totalCount
        val selectedVault = when (vaultResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, "Cannot get vault")
                PassLogger.w(TAG, vaultResult.exception)
                return@combineN VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combineN VaultOptionsUiState.Loading
            is LoadingResult.Success -> {
                val selectedVault = vaultResult.data.find { it.shareId == navShareId }
                if (selectedVault == null) {
                    snackbarDispatcher(CannotFindVaultError)
                    PassLogger.w(TAG, "Cannot find vault with shareId $navShareId")
                    return@combineN VaultOptionsUiState.Error
                }
                selectedVault
            }
        }

        val isUpgradeAvailable = upgradeResult.getOrNull()?.isUpgradeAvailable ?: false
        val canDelete = canDeleteVault(selectedVault)

        val canEdit = selectedVault.canBeUpdated
        val canMigrate = canMigrateVault(navShareId)
        val canLeave = if (selectedVault.isGroupShare) false else !selectedVault.isOwned

        val vaultAccessData = canManageVaultAccess(selectedVault)

        // Only show share if the user is a manager (canShare already gates on Owner/Admin role)
        val showShare = canShare

        // Non-owners are always in a shared context (vault was shared with them).
        // Owners only see member options if they've shared the vault with others.
        val isSharedContext = selectedVault.shared || !selectedVault.isOwned
        val showManageAccess = isSharedContext && vaultAccessData.canManageAccess
        val showViewMembers = isSharedContext && vaultAccessData.canViewMembers

        VaultOptionsUiState.Success(
            shareId = navShareId,
            showEdit = canEdit,
            showMigrate = canMigrate,
            showDelete = canDelete,
            showShare = showShare,
            showLeave = canLeave,
            showManageAccess = showManageAccess,
            showViewMembers = showViewMembers,
            event = event,
            isLastVault = vaultResult.data.size == 1,
            canAddFolder = rootFolderCount < folderLimits.maxChildren &&
                totalFolderCount < folderLimits.maxCount &&
                (canCreateFolder.isAllowed || canCreateFolder.needsUpgrade && isUpgradeAvailable),
            canAddFolderNeedsUpgrade = rootFolderCount < folderLimits.maxChildren &&
                totalFolderCount < folderLimits.maxCount &&
                canCreateFolder.needsUpgrade && isUpgradeAvailable,
            showOfflineAttachments = offlineAttachmentsFF && downloadAllPref.value(),
            isOfflineAttachmentsEnabled = selectedVault.offlineAttachments
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = VaultOptionsUiState.Uninitialised
    )

    internal fun onEventConsumed(event: VaultOptionsEvent) {
        eventFlow.compareAndSet(event, VaultOptionsEvent.Idle)
    }

    internal fun onToggleOfflineAttachments(enabled: Boolean) {
        viewModelScope.launch {
            val userId = observePrimaryUserId().firstOrNull() ?: return@launch
            safeRunCatching { setVaultOfflineAttachments(userId, navShareId, enabled) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Failed to toggle offline attachments")
                    PassLogger.w(TAG, error)
                }
        }
    }

    internal fun onMigrateVault() {
        viewModelScope.launch {
            safeRunCatching {
                observeEncryptedItems(
                    selection = ShareSelection.Share(navShareId),
                    itemState = ItemState.Active,
                    filter = ItemTypeFilter.All,
                    includeHidden = true
                )
                    .first()
                    .any { itemEncrypted -> itemEncrypted.isShared }
            }.onFailure { error ->
                PassLogger.w(TAG, "Failed to check vault items before migrating")
                PassLogger.w(TAG, error)
                snackbarDispatcher(CannotMigrateVaultError)
            }.onSuccess { hasSharedItems ->
                val event = if (hasSharedItems) {
                    VaultOptionsEvent.OnMigrateVaultItemsSharedWarning(navShareId)
                } else {
                    VaultOptionsEvent.OnMigrateVaultItems(navShareId)
                }
                eventFlow.update { event }
            }
        }
    }

    private fun canDeleteVault(selectedVault: Vault): Boolean = selectedVault.isOwned

    private companion object {
        private const val TAG = "VaultOptionsViewModel"
    }

}
