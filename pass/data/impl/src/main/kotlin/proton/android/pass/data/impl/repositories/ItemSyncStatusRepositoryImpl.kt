package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import javax.inject.Inject

class ItemSyncStatusRepositoryImpl @Inject constructor() : ItemSyncStatusRepository {

    private val syncStatus = MutableSharedFlow<ItemSyncStatus>()

    override fun emit(status: ItemSyncStatus) {
        syncStatus.tryEmit(status)
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = syncStatus.distinctUntilChanged()

}
