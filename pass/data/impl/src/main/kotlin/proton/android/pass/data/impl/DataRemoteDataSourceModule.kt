package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.remote.RemoteAliasDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteEventDataSource
import proton.android.pass.data.impl.remote.RemoteEventDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import proton.android.pass.data.impl.remote.RemoteImageFetcherImpl
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.data.impl.remote.RemotePlanDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRemoteDataSourceModule {

    @Binds
    abstract fun bindRemoteAliasDataSource(impl: RemoteAliasDataSourceImpl): RemoteAliasDataSource

    @Binds
    abstract fun bindRemoteEventDataSource(impl: RemoteEventDataSourceImpl): RemoteEventDataSource

    @Binds
    abstract fun bindRemoteItemDataSource(impl: RemoteItemDataSourceImpl): RemoteItemDataSource

    @Binds
    abstract fun bindRemoteItemKeyDataSource(impl: RemoteItemKeyDataSourceImpl): RemoteItemKeyDataSource

    @Binds
    abstract fun bindRemoteShareDataSource(impl: RemoteShareDataSourceImpl): RemoteShareDataSource

    @Binds
    abstract fun bindRemoteShareKeyDataSource(impl: RemoteShareKeyDataSourceImpl): RemoteShareKeyDataSource

    @Binds
    abstract fun bindRemoteImageFetcher(impl: RemoteImageFetcherImpl): RemoteImageFetcher

    @Binds
    abstract fun bindRemoteTelemetryDataSource(
        impl: RemoteTelemetryDataSourceImpl
    ): RemoteTelemetryDataSource

    @Binds
    abstract fun bindRemotePlanDataSource(
        impl: RemotePlanDataSourceImpl
    ): RemotePlanDataSource
}

