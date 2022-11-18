package me.proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.data.impl.remote.RemoteAliasDataSource
import me.proton.android.pass.data.impl.remote.RemoteAliasDataSourceImpl
import me.proton.android.pass.data.impl.remote.RemoteItemDataSource
import me.proton.android.pass.data.impl.remote.RemoteItemDataSourceImpl
import me.proton.android.pass.data.impl.remote.RemoteKeyPacketDataSource
import me.proton.android.pass.data.impl.remote.RemoteKeyPacketDataSourceImpl
import me.proton.android.pass.data.impl.remote.RemoteShareDataSource
import me.proton.android.pass.data.impl.remote.RemoteShareDataSourceImpl
import me.proton.android.pass.data.impl.remote.RemoteVaultItemKeyDataSource
import me.proton.android.pass.data.impl.remote.RemoteVaultItemKeyDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRemoteDataSourceModule {

    @Binds
    abstract fun bindRemoteAliasDataSource(impl: RemoteAliasDataSourceImpl): RemoteAliasDataSource

    @Binds
    abstract fun bindRemoteItemDataSource(impl: RemoteItemDataSourceImpl): RemoteItemDataSource

    @Binds
    abstract fun bindRemoteShareDataSource(impl: RemoteShareDataSourceImpl): RemoteShareDataSource

    @Binds
    abstract fun bindRemoteKeyPacketDataSource(impl: RemoteKeyPacketDataSourceImpl): RemoteKeyPacketDataSource

    @Binds
    abstract fun bindRemoteVaultItemKeyDataSource(impl: RemoteVaultItemKeyDataSourceImpl): RemoteVaultItemKeyDataSource

}

