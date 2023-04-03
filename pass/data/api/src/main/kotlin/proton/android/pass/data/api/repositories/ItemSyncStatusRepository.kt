package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow

sealed interface ItemSyncStatus {
    object NotStarted : ItemSyncStatus
    object Syncing : ItemSyncStatus
    data class Synced(val hasItems: Boolean) : ItemSyncStatus
    object NotSynced : ItemSyncStatus
}

interface ItemSyncStatusRepository {
    fun emit(status: ItemSyncStatus)
    fun observeSyncStatus(): Flow<ItemSyncStatus>
}
