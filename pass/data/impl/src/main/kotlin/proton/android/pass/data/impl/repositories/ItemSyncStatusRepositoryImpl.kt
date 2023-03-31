package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemSyncStatusRepositoryImpl @Inject constructor() : ItemSyncStatusRepository {

    private val syncStatus = MutableSharedFlow<ItemSyncStatus>(replay = 1)
    private val observedSyncStatus = syncStatus.onStart { emit(ItemSyncStatus.Synced) }

    override fun emit(status: ItemSyncStatus) {
        syncStatus.tryEmit(status)
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = observedSyncStatus.distinctUntilChanged()

}
