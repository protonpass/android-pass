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
import proton.android.pass.data.api.core.repositories.SentinelRepository
import proton.android.pass.data.api.repositories.AliasContactsRepository
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.InAppMessagesRepository
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.LiveTelemetryRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.repositories.OrganizationSettingsRepository
import proton.android.pass.data.api.repositories.ReportRepository
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.core.repositories.SentinelRepositoryImpl
import proton.android.pass.data.impl.repositories.AliasContactsRepositoryImpl
import proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import proton.android.pass.data.impl.repositories.AssetLinkRepositoryImpl
import proton.android.pass.data.impl.repositories.BreachRepositoryImpl
import proton.android.pass.data.impl.repositories.BulkInviteRepositoryImpl
import proton.android.pass.data.impl.repositories.BulkMoveToVaultRepositoryImpl
import proton.android.pass.data.impl.repositories.DraftAttachmentRepositoryImpl
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.repositories.EventRepositoryImpl
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import proton.android.pass.data.impl.repositories.ExtraPasswordRepositoryImpl
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepository
import proton.android.pass.data.impl.repositories.FetchShareItemsStatusRepositoryImpl
import proton.android.pass.data.impl.repositories.InAppMessagesRepositoryImpl
import proton.android.pass.data.impl.repositories.InviteRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.data.impl.repositories.ItemKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemRepositoryImpl
import proton.android.pass.data.impl.repositories.ItemSyncStatusRepositoryImpl
import proton.android.pass.data.impl.repositories.LiveTelemetryRepositoryImpl
import proton.android.pass.data.impl.repositories.MetadataResolverImpl
import proton.android.pass.data.impl.repositories.OnMemoryDraftRepository
import proton.android.pass.data.impl.repositories.OrganizationSettingsRepositoryImpl
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.android.pass.data.impl.repositories.PlanRepositoryImpl
import proton.android.pass.data.impl.repositories.ReportRepositoryImpl
import proton.android.pass.data.impl.repositories.SearchEntryRepositoryImpl
import proton.android.pass.data.impl.repositories.SecureLinkRepository
import proton.android.pass.data.impl.repositories.SecureLinkRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.data.impl.repositories.ShareKeyRepositoryImpl
import proton.android.pass.data.impl.repositories.ShareRepositoryImpl
import proton.android.pass.data.impl.repositories.SimpleLoginRepositoryImpl
import proton.android.pass.data.impl.repositories.TelemetryRepositoryImpl
import proton.android.pass.data.impl.repositories.UserAccessDataRepositoryImpl
import javax.inject.Singleton

@Suppress("TooManyFunctions")
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

    @Binds
    abstract fun bindInviteRepository(impl: InviteRepositoryImpl): InviteRepository

    @Binds
    abstract fun bindFetchShareItemStatusRepository(
        impl: FetchShareItemsStatusRepositoryImpl
    ): FetchShareItemsStatusRepository

    @Binds
    abstract fun bindUserAccessDataRepository(impl: UserAccessDataRepositoryImpl): UserAccessDataRepository

    @Binds
    abstract fun bindBulkMoveToVaultRepository(impl: BulkMoveToVaultRepositoryImpl): BulkMoveToVaultRepository

    @Binds
    abstract fun bindBulkInviteRepository(impl: BulkInviteRepositoryImpl): BulkInviteRepository

    @Binds
    abstract fun bindOrganizationSettingsRepository(
        impl: OrganizationSettingsRepositoryImpl
    ): OrganizationSettingsRepository

    @[Binds Singleton]
    abstract fun bindSentinelRepository(impl: SentinelRepositoryImpl): SentinelRepository

    @Binds
    abstract fun bindBreachRepository(impl: BreachRepositoryImpl): BreachRepository

    @Binds
    abstract fun bindSecureLinkRepository(impl: SecureLinkRepositoryImpl): SecureLinkRepository

    @Binds
    abstract fun bindAccessKeyRepository(impl: ExtraPasswordRepositoryImpl): ExtraPasswordRepository

    @Binds
    abstract fun bindLiveTelemetryRepository(impl: LiveTelemetryRepositoryImpl): LiveTelemetryRepository

    @Binds
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @[Binds Singleton]
    abstract fun bindSimpleLoginRepository(impl: SimpleLoginRepositoryImpl): SimpleLoginRepository

    @[Binds Singleton]
    abstract fun bindAssetLinkRepository(impl: AssetLinkRepositoryImpl): AssetLinkRepository

    @[Binds Singleton]
    abstract fun bindAliasContactsRepository(impl: AliasContactsRepositoryImpl): AliasContactsRepository

    @[Binds Singleton]
    abstract fun bindInAppMessagesRepository(impl: InAppMessagesRepositoryImpl): InAppMessagesRepository

    @[Binds Singleton]
    abstract fun bindDraftAttachmentRepository(impl: DraftAttachmentRepositoryImpl): DraftAttachmentRepository

    @[Binds Singleton]
    abstract fun bindMetadataResolver(impl: MetadataResolverImpl): MetadataResolver

}
