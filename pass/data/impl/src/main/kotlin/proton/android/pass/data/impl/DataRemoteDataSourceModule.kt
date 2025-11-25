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
import proton.android.pass.data.impl.remote.RemoteAliasContactsDataSource
import proton.android.pass.data.impl.remote.RemoteAliasContactsDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteAliasDataSource
import proton.android.pass.data.impl.remote.RemoteAliasDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.remote.RemoteBreachDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteEventDataSource
import proton.android.pass.data.impl.remote.RemoteEventDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSource
import proton.android.pass.data.impl.remote.RemoteExtraPasswordDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import proton.android.pass.data.impl.remote.RemoteImageFetcherImpl
import proton.android.pass.data.impl.remote.RemoteInviteDataSource
import proton.android.pass.data.impl.remote.RemoteInviteDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemDataSource
import proton.android.pass.data.impl.remote.RemoteItemDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSource
import proton.android.pass.data.impl.remote.RemoteItemKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteLiveTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteLiveTelemetryDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteOrganizationReportDataSource
import proton.android.pass.data.impl.remote.RemoteOrganizationReportDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteOrganizationSettingsDataSource
import proton.android.pass.data.impl.remote.RemoteOrganizationSettingsDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSource
import proton.android.pass.data.impl.remote.RemoteSecureLinkDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareDataSource
import proton.android.pass.data.impl.remote.RemoteShareDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSource
import proton.android.pass.data.impl.remote.RemoteShareKeyDataSourceImpl
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSource
import proton.android.pass.data.impl.remote.RemoteTelemetryDataSourceImpl
import proton.android.pass.data.impl.remote.accessdata.RemoteUserAccessDataDataSource
import proton.android.pass.data.impl.remote.accessdata.RemoteUserAccessDataDataSourceImpl
import proton.android.pass.data.impl.remote.assetlink.RemoteAssetLinkDataSource
import proton.android.pass.data.impl.remote.assetlink.RemoteAssetLinkDataSourceImpl
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSource
import proton.android.pass.data.impl.remote.attachments.RemoteAttachmentsDataSourceImpl
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSource
import proton.android.pass.data.impl.remote.inappmessages.RemoteInAppMessagesDataSourceImpl
import proton.android.pass.data.impl.remote.shares.RemoteShareInvitesDataSource
import proton.android.pass.data.impl.remote.shares.RemoteShareInvitesDataSourceImpl
import proton.android.pass.data.impl.remote.shares.RemoteShareMembersDataSource
import proton.android.pass.data.impl.remote.shares.RemoteShareMembersDataSourceImpl
import proton.android.pass.data.impl.remote.simplelogin.RemoteSimpleLoginDataSource
import proton.android.pass.data.impl.remote.simplelogin.RemoteSimpleLoginDataSourceImpl
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
    abstract fun bindRemoteInviteDataSource(impl: RemoteInviteDataSourceImpl): RemoteInviteDataSource

    @Binds
    abstract fun bindRemoteOrganizationSettingsDataSource(
        impl: RemoteOrganizationSettingsDataSourceImpl
    ): RemoteOrganizationSettingsDataSource

    @[Binds Singleton]
    abstract fun bindRemoteSentinelDataSource(impl: RemoteSentinelDataSourceImpl): RemoteSentinelDataSource

    @Binds
    abstract fun bindRemoteBreachDataSource(impl: RemoteBreachDataSourceImpl): RemoteBreachDataSource

    @Binds
    abstract fun bindRemotePublicLinkDataSource(impl: RemoteSecureLinkDataSourceImpl): RemoteSecureLinkDataSource

    @Binds
    abstract fun bindRemoteAccessKeyDataSource(impl: RemoteExtraPasswordDataSourceImpl): RemoteExtraPasswordDataSource

    @Binds
    abstract fun bindRemoteLiveTelemetryDataSource(
        impl: RemoteLiveTelemetryDataSourceImpl
    ): RemoteLiveTelemetryDataSource

    @[Binds Singleton]
    abstract fun bindRemoteSimpleLoginDataSource(impl: RemoteSimpleLoginDataSourceImpl): RemoteSimpleLoginDataSource

    @[Binds Singleton]
    abstract fun bindRemoteUserAccessDataDataSource(
        impl: RemoteUserAccessDataDataSourceImpl
    ): RemoteUserAccessDataDataSource

    @[Binds Singleton]
    abstract fun bindRemoteAssetLinkDataSource(impl: RemoteAssetLinkDataSourceImpl): RemoteAssetLinkDataSource

    @[Binds Singleton]
    abstract fun bindRemoteAliasContactsDataSource(
        impl: RemoteAliasContactsDataSourceImpl
    ): RemoteAliasContactsDataSource

    @[Binds Singleton]
    abstract fun bindRemoteInAppMessagesDataSource(
        impl: RemoteInAppMessagesDataSourceImpl
    ): RemoteInAppMessagesDataSource

    @[Binds Singleton]
    abstract fun bindRemoteAttachmentsDataSource(impl: RemoteAttachmentsDataSourceImpl): RemoteAttachmentsDataSource

    @[Binds Singleton]
    abstract fun bindRemoteShareMembersDataSource(impl: RemoteShareMembersDataSourceImpl): RemoteShareMembersDataSource

    @[Binds Singleton]
    abstract fun bindRemoteShareInvitesDataSource(impl: RemoteShareInvitesDataSourceImpl): RemoteShareInvitesDataSource

    @[Binds Singleton]
    abstract fun bindRemoteOrganizationReportDataSource(
        impl: RemoteOrganizationReportDataSourceImpl
    ): RemoteOrganizationReportDataSource

}

