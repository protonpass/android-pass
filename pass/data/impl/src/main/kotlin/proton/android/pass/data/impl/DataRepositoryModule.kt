package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.repositories.EventRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemSyncStatusRepositoryImpl
import proton.android.pass.data.impl.repositories.OnMemoryDraftRepository
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.android.pass.data.impl.repositories.PlanRepositoryImpl
import proton.android.pass.data.impl.repositories.SearchEntryRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.repositories.ShareKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareRepositoryImpl
import proton.android.pass.data.impl.repositories.TelemetryRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryModule {

    @Binds
    abstract fun bindAliasRepository(impl: AliasRepositoryImpl): AliasRepository

    @Binds
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    abstract fun bindItemKeyRepository(impl: ItemKeyRepositoryImpl): ItemKeyRepository

    @Binds
    abstract fun bindShareKeyRepository(impl: ShareKeyRepositoryImpl): ShareKeyRepository

    @Binds
    abstract fun bindShareRepository(impl: ShareRepositoryImpl): ShareRepository

    @Binds
    abstract fun bindTelemetryRepository(impl: TelemetryRepositoryImpl): TelemetryRepository

    @Binds
    abstract fun bindSearchEntryRepository(impl: SearchEntryRepositoryImpl): SearchEntryRepository

    @Binds
    abstract fun bindDraftRepository(impl: OnMemoryDraftRepository): DraftRepository

    @Binds
    abstract fun bindItemSyncStatusRepository(impl: ItemSyncStatusRepositoryImpl): ItemSyncStatusRepository

    @Binds
    abstract fun bindPlanRepository(impl: PlanRepositoryImpl): PlanRepository
}
