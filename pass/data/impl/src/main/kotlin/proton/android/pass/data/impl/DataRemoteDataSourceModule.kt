/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.data.api.core.datasources.RemoteSentinelDataSource
import proton.android.pass.data.impl.core.datasources.RemoteSentinelDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.remote.RemoteAliasDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.remote.RemoteBreachDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteEventDataSource
import proton.android.pass.data.impl.remote.RemoteEventDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import proton.android.pass.data.impl.remote.RemoteImageFetcherImpl
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.remote.RemoteInviteDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteOrganizationSettingsDataSource
import proton.android.pass.data.impl.remote.RemoteOrganizationSettingsDataSourceImpl
import proton.android.pass.data.impl.remote.RemotePlanDataSource
import proton.android.pass.data.impl.remote.RemotePlanDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSourceImpl
import javax.inject.Singleton

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
    abstract fun bindRemoteTelemetryDataSource(impl: RemoteTelemetryDataSourceImpl): RemoteTelemetryDataSource

    @Binds
    abstract fun bindRemotePlanDataSource(impl: RemotePlanDataSourceImpl): RemotePlanDataSource

    @Binds
    abstract fun bindRemoteInviteDataSource(impl: RemoteInviteDataSourceImpl): RemoteInviteDataSource

    @Binds
    abstract fun bindRemoteOrganizationSettingsDataSource(
        impl: RemoteOrganizationSettingsDataSourceImpl
    ): RemoteOrganizationSettingsDataSource

    @[Binds Singleton]
    abstract fun bindRemoteSentinelDataSource(impl: RemoteSentinelDataSourceImpl): RemoteSentinelDataSource

    @Binds
    abstract fun bindRemoteBreachDataSource(impl: RemoteBreachDataSourceImpl): RemoteBreachDataSource
}

