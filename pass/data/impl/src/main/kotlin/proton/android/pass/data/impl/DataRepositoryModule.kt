package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.repositories.EventRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.repositories.ShareKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareRepositoryImpl

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

}
