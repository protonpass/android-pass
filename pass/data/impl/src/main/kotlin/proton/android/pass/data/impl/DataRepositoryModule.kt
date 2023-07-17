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
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.FeatureFlagRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.TelemetryRepository
import proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import proton.android.pass.data.impl.repositories.EventRepository
import proton.android.pass.data.impl.repositories.EventRepositoryImpl
import proton.android.pass.data.impl.repositories.FeatureFlagRepositoryImpl
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

    @Binds
    abstract fun bindFeatureFlagsRepository(impl: FeatureFlagRepositoryImpl): FeatureFlagRepository
}
