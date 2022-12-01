package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.repositories.KeyPacketRepository
import me.proton.android.pass.data.api.repositories.ShareRepository
import me.proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import me.proton.android.pass.data.impl.repositories.EventRepository
import me.proton.android.pass.data.impl.repositories.EventRepositoryImpl
import me.proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import me.proton.android.pass.data.impl.repositories.KeyPacketRepositoryImpl
import me.proton.android.pass.data.impl.repositories.ShareRepositoryImpl
import me.proton.android.pass.data.impl.repositories.VaultKeyRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryModule {

    @Binds
    abstract fun bindAliasRepository(impl: AliasRepositoryImpl): AliasRepository

    @Binds
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    abstract fun bindKeyPacketRepository(impl: KeyPacketRepositoryImpl): KeyPacketRepository

    @Binds
    abstract fun bindShareRepository(impl: ShareRepositoryImpl): ShareRepository

    @Binds
    abstract fun bindVaultKeyRepository(impl: VaultKeyRepositoryImpl): VaultKeyRepository

    @Binds
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

}
