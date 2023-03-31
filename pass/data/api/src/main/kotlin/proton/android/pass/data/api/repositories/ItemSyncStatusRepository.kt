package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow

sealed interface ItemSyncStatus {
    object Syncing : ItemSyncStatus
    object Synced : ItemSyncStatus
    object NotSynced : ItemSyncStatus
}

interface ItemSyncStatusRepository {
    fun emit(status: ItemSyncStatus)
    fun observeSyncStatus(): Flow<ItemSyncStatus>
}
