package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSyncStatusRepositoryImpl @Inject constructor() : ItemSyncStatusRepository {

    private val syncStatus = MutableStateFlow<ItemSyncStatus>(ItemSyncStatus.NotStarted)

    override fun emit(status: ItemSyncStatus) {
        syncStatus.update { status }
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = syncStatus

}
