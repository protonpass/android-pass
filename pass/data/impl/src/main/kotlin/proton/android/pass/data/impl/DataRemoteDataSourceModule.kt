package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.remote.RemoteAliasDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteEventDataSource
import proton.android.pass.data.impl.remote.RemoteEventDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteKeyPacketDataSource
import proton.android.pass.data.impl.remote.RemoteKeyPacketDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteVaultItemKeyDataSource
import proton.android.pass.data.impl.remote.RemoteVaultItemKeyDataSourceImpl

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

    @Binds
    abstract fun bindRemoteEventDataSource(impl: RemoteEventDataSourceImpl): RemoteEventDataSource
}

