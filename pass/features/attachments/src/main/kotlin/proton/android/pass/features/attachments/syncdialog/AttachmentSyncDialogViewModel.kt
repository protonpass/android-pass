/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.attachments.syncdialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.attachments.ObserveActiveAttachments
import proton.android.pass.data.api.usecases.attachments.ObserveOfflineEnabledShareIds
import proton.android.pass.data.api.usecases.attachments.RefreshAttachmentsForItems
import proton.android.pass.data.api.usecases.attachments.UpdateAttachmentDownloadStatus
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.data.api.usecases.attachments.GetVaultUsage
import proton.android.pass.features.attachments.syncdialog.AttachmentSyncDialogState.SyncStatus
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import proton.android.pass.composecomponents.impl.R as ComposeR

@HiltViewModel
class AttachmentSyncDialogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountManager: AccountManager,
    private val observeOfflineEnabledShareIds: ObserveOfflineEnabledShareIds,
    private val observeVaults: ObserveVaults,
    private val observeItems: ObserveItems,
    private val networkMonitor: NetworkMonitor,
    private val observeActiveAttachments: ObserveActiveAttachments,
    private val updateAttachmentDownloadStatus: UpdateAttachmentDownloadStatus,
    private val refreshAttachmentsForItems: RefreshAttachmentsForItems,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val downloadScheduler: AttachmentDownloadScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getVaultUsage: GetVaultUsage
) : ViewModel() {

    private val refreshFailedItemsState = MutableStateFlow<Set<ItemId>>(emptySet())

    private val vaultSizesState = MutableStateFlow<Map<ShareId, Long>>(emptyMap())
    private val inFlightShareIds = mutableSetOf<ShareId>()

    internal val state: StateFlow<AttachmentSyncDialogState> = accountManager
        .getPrimaryUserId()
        .flatMapLatest { userId ->
            userId?.let(::observeSyncState) ?: flowOf(AttachmentSyncDialogState.Empty)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AttachmentSyncDialogState.Initial
        )

    init {
        viewModelScope.launch {
            refreshAttachmentsForItemsWithFlag()
        }
    }

    internal fun onRetryAttachment(attachment: Attachment) {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
            runCatching {
                updateAttachmentDownloadStatus(userId, attachment, AttachmentDownloadStatus.Downloading)
            }.onError { error ->
                PassLogger.w(TAG, "Error retrying attachment download")
                PassLogger.w(TAG, error)
            }
            refreshFailedItemsState.update { it - attachment.itemId }
            safeRunCatching {
                downloadScheduler.scheduleAttachment(
                    userId = userId,
                    shareId = attachment.shareId,
                    itemId = attachment.itemId,
                    attachmentId = attachment.id
                )
            }.onFailure { error ->
                PassLogger.w(TAG, "Error rescheduling download on retry")
                PassLogger.w(TAG, error)
            }
        }
    }

    internal fun onRetryAll() {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return@launch
            refreshFailedItemsState.update { emptySet() }
            safeRunCatching { downloadScheduler.scheduleAll(userId, force = true) }
                .onFailure { error ->
                    PassLogger.w(TAG, "Error rescheduling downloads on retry all")
                    PassLogger.w(TAG, error)
                }
        }
    }

    private fun observeSyncState(userId: UserId): Flow<AttachmentSyncDialogState> =
        observeOfflineEnabledShareIds(userId).flatMapLatest { shareIds ->
            if (shareIds.isEmpty()) {
                flowOf(AttachmentSyncDialogState.Empty)
            } else {
                observeSyncStateForShares(userId, shareIds)
            }
        }

    private fun observeSyncStateForShares(userId: UserId, shareIds: List<ShareId>): Flow<AttachmentSyncDialogState> =
        combine(
            observeNetworkInfo(userId),
            observeVaultsWithSizes(userId),
            observeAttachmentsWithFailures(userId, shareIds),
            observeItemsForShares(userId, shareIds),
            userPreferencesRepository.observeSharedItemsDownloadPref(userId)
        ) { networkInfo, (vaults, sizes), (attachments, failedItems), items, sharedItemsPref ->
            val offlineVaults = vaults.filter { it.offlineAttachments }
            val itemInfoMap = buildItemInfoMap(items)

            val vaultStates = buildVaultStates(
                offlineVaults = offlineVaults,
                sizes = sizes,
                attachmentsByShareId = attachments.groupBy { it.shareId },
                itemInfoMap = itemInfoMap,
                failedItems = failedItems
            )

            val sharedItemsState = if (sharedItemsPref.value()) {
                buildSharedItemsState(
                    attachments = attachments,
                    offlineVaultShareIds = offlineVaults.map { it.shareId }.toSet(),
                    itemInfoMap = itemInfoMap,
                    failedItems = failedItems
                )
            } else {
                null
            }

            buildDialogState(
                vaults = vaultStates + listOfNotNull(sharedItemsState),
                networkInfo = networkInfo,
                failedItems = failedItems
            )
        }

    private fun observeNetworkInfo(userId: UserId): Flow<NetworkInfo> = combine(
        networkMonitor.connectivity,
        networkMonitor.isMetered,
        userPreferencesRepository.observeAllowCellularDownloadPref(userId)
    ) { status, isMetered, allowCellular ->
        NetworkInfo(
            status = status,
            isMetered = isMetered,
            allowCellular = allowCellular.value()
        )
    }

    private fun observeVaultsWithSizes(userId: UserId): Flow<Pair<List<Vault>, Map<ShareId, Long>>> = combine(
        observeVaults(userId = userId, includeHidden = true)
            .onEach { vaults ->
                loadVaultSizes(
                    userId = userId,
                    shareIds = vaults.filter { it.offlineAttachments }.map { it.shareId }
                )
            },
        vaultSizesState
    ) { vaults, sizes -> vaults to sizes }

    private fun observeAttachmentsWithFailures(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<Pair<List<Attachment>, Set<ItemId>>> = combine(
        observeActiveAttachments(userId, shareIds),
        refreshFailedItemsState
    ) { attachments, failedItems -> attachments to failedItems }

    private fun observeItemsForShares(userId: UserId, shareIds: List<ShareId>): Flow<List<Item>> = observeItems(
        selection = ShareSelection.Shares(shareIds),
        itemState = ItemState.Active,
        filter = ItemTypeFilter.All,
        userId = userId,
        includeHidden = true
    )

    private suspend fun buildItemInfoMap(items: List<Item>): Map<ItemKey, ItemInfo> =
        encryptionContextProvider.withEncryptionContextSuspendable {
            items.associate { item ->
                ItemKey(shareId = item.shareId, itemId = item.id) to
                    ItemInfo(name = decrypt(item.title), category = item.itemType.category)
            }
        }

    private fun buildVaultStates(
        offlineVaults: List<Vault>,
        sizes: Map<ShareId, Long>,
        attachmentsByShareId: Map<ShareId, List<Attachment>>,
        itemInfoMap: Map<ItemKey, ItemInfo>,
        failedItems: Set<ItemId>
    ): List<VaultSyncState> = offlineVaults.map { vault ->
        val itemStates = attachmentsByShareId[vault.shareId]
            .orEmpty()
            .groupBy { it.itemId }
            .mapNotNull { (itemId, itemAttachments) ->
                buildItemState(vault.shareId, itemId, itemAttachments, itemInfoMap, failedItems)
            }

        VaultSyncState(
            shareId = vault.shareId,
            name = vault.name,
            color = vault.color,
            icon = vault.icon,
            items = itemStates,
            totalSizeBytes = sizes[vault.shareId]
        )
    }.filter { it.items.isNotEmpty() }

    private fun buildSharedItemsState(
        attachments: List<Attachment>,
        offlineVaultShareIds: Set<ShareId>,
        itemInfoMap: Map<ItemKey, ItemInfo>,
        failedItems: Set<ItemId>
    ): VaultSyncState? {
        val itemStates = attachments
            .filter { it.shareId !in offlineVaultShareIds }
            .groupBy { it.itemId }
            .mapNotNull { (itemId, itemAttachments) ->
                buildItemState(
                    shareId = itemAttachments.first().shareId,
                    itemId = itemId,
                    attachments = itemAttachments,
                    itemInfoMap = itemInfoMap,
                    failedItems = failedItems
                )
            }

        return if (itemStates.isEmpty()) {
            null
        } else {
            VaultSyncState(
                shareId = ShareId(SHARED_WITH_ME_ID),
                name = context.getString(ComposeR.string.item_list_header_shared_with_me),
                color = ShareColor.Color1,
                icon = ShareIcon.Icon9,
                items = itemStates
            )
        }
    }

    private fun buildItemState(
        shareId: ShareId,
        itemId: ItemId,
        attachments: List<Attachment>,
        itemInfoMap: Map<ItemKey, ItemInfo>,
        failedItems: Set<ItemId>
    ): ItemSyncState? {
        val info = itemInfoMap[ItemKey(shareId, itemId)] ?: return null
        val refreshFailed = itemId in failedItems
        return ItemSyncState(
            itemId = itemId,
            shareId = shareId,
            name = info.name,
            itemCategory = info.category,
            attachments = attachments.map { it.toRowState(refreshFailed) }
        )
    }

    private fun buildDialogState(
        vaults: List<VaultSyncState>,
        networkInfo: NetworkInfo,
        failedItems: Set<ItemId>
    ): AttachmentSyncDialogState {
        val allAttachments = vaults.flatMap { vault -> vault.items.flatMap { it.attachments } }
        val total = allAttachments.size
        val downloaded = allAttachments.count { it.status == AttachmentDownloadStatus.Downloaded }
        val failed = allAttachments.count { it.status == AttachmentDownloadStatus.Failed }

        return AttachmentSyncDialogState(
            status = resolveSyncStatus(networkInfo, failedItems, total, downloaded, failed),
            downloaded = downloaded,
            total = total,
            vaults = vaults
        )
    }

    private fun resolveSyncStatus(
        networkInfo: NetworkInfo,
        failedItems: Set<ItemId>,
        total: Int,
        downloaded: Int,
        failed: Int
    ): SyncStatus {
        val pending = total - downloaded - failed
        val blockedByNetwork = pending > 0 &&
            networkInfo.status == NetworkStatus.Online &&
            networkInfo.isMetered &&
            !networkInfo.allowCellular

        return when {
            networkInfo.status == NetworkStatus.Offline || blockedByNetwork -> SyncStatus.WaitingForConnection
            failedItems.isNotEmpty() && total == 0 -> SyncStatus.PartialFailure
            total == 0 -> SyncStatus.Done
            failed > 0 && downloaded + failed >= total -> SyncStatus.PartialFailure
            downloaded >= total -> SyncStatus.Done
            else -> SyncStatus.Downloading
        }
    }

    private fun loadVaultSizes(userId: UserId, shareIds: List<ShareId>) {
        val knownShareIds = vaultSizesState.value.keys
        val missing = shareIds.filter { shareId ->
            shareId !in knownShareIds && shareId !in inFlightShareIds
        }
        if (missing.isEmpty()) return
        inFlightShareIds.addAll(missing)
        viewModelScope.launch {
            try {
                coroutineScope {
                    val results = missing.map { shareId ->
                        async {
                            val size = safeRunCatching { getVaultUsage(userId, shareId) }
                                .onFailure { error ->
                                    PassLogger.w(TAG, "Error fetching usage for vault ${shareId.id}")
                                    PassLogger.w(TAG, error)
                                }
                                .getOrNull()
                            shareId to size
                        }
                    }.awaitAll()
                    val successes = results.mapNotNull { (id, size) -> size?.let { id to it } }
                    if (successes.isNotEmpty()) {
                        vaultSizesState.update { current -> current + successes }
                    }
                }
            } finally {
                inFlightShareIds.removeAll(missing.toSet())
            }
        }
    }

    private suspend fun refreshAttachmentsForItemsWithFlag() {
        val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return
        val shareIds = observeOfflineEnabledShareIds(userId).firstOrNull()
            ?: return
        if (shareIds.isEmpty()) return

        val items = observeItems(
            selection = ShareSelection.Shares(shareIds),
            itemState = ItemState.Active,
            filter = ItemTypeFilter.All,
            userId = userId,
            itemFlags = mapOf(ItemFlag.HasAttachments to true),
            includeHidden = true
        ).firstOrNull().orEmpty()

        val itemPairs = items.map { it.shareId to it.id }
        if (itemPairs.isNotEmpty()) {
            runCatching { refreshAttachmentsForItems(userId, itemPairs) }
                .onSuccess { failed ->
                    if (failed.isNotEmpty()) {
                        refreshFailedItemsState.update { current -> current + failed.map { it.second } }
                    }
                }
                .onError { error ->
                    PassLogger.w(TAG, "Error refreshing attachments for items with flag")
                    PassLogger.w(TAG, error)
                    refreshFailedItemsState.update { current -> current + itemPairs.map { it.second } }
                }
        }
    }

    private data class ItemKey(val shareId: ShareId, val itemId: ItemId)

    private data class ItemInfo(val name: String, val category: ItemCategory)

    private data class NetworkInfo(
        val status: NetworkStatus,
        val isMetered: Boolean,
        val allowCellular: Boolean
    )

    internal companion object {

        private const val TAG = "AttachmentSyncDialogViewModel"
        internal const val SHARED_WITH_ME_ID = "shared_with_me"
    }
}

private fun Attachment.toRowState(refreshFailed: Boolean) = AttachmentSyncRowState(
    attachment = this,
    name = name,
    size = size,
    status = if (refreshFailed && downloadStatus != AttachmentDownloadStatus.Downloaded) {
        AttachmentDownloadStatus.Failed
    } else {
        downloadStatus
    }
)
