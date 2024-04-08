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
import proton.android.pass.data.api.core.datasources.LocalSentinelDataSource
import proton.android.pass.data.impl.core.datasources.LocalSentinelDataSourceImpl
import proton.android.pass.data.impl.local.LocalDataMigrationDataSource
import proton.android.pass.data.impl.local.LocalDataMigrationDataSourceImpl
import proton.android.pass.data.impl.local.LocalEventDataSource
import proton.android.pass.data.impl.local.LocalEventDataSourceImpl
import proton.android.pass.data.impl.local.LocalInviteDataSource
import proton.android.pass.data.impl.local.LocalInviteDataSourceImpl
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.local.LocalItemDataSourceImpl
import proton.android.pass.data.impl.local.LocalOrganizationSettingsDataSource
import proton.android.pass.data.impl.local.LocalOrganizationSettingsDataSourceImpl
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.local.LocalPlanDataSourceImpl
import proton.android.pass.data.impl.local.LocalSearchEntryDataSource
import proton.android.pass.data.impl.local.LocalSearchEntryDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.local.LocalShareDataSourceImpl
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.data.impl.local.LocalShareKeyDataSourceImpl
import proton.android.pass.data.impl.local.LocalTelemetryDataSource
import proton.android.pass.data.impl.local.LocalTelemetryDataSourceImpl
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataLocalDataSourceModule {

    @Binds
    abstract fun bindLocalItemDataSource(impl: LocalItemDataSourceImpl): LocalItemDataSource

    @Binds
    abstract fun bindLocalShareDataSource(impl: LocalShareDataSourceImpl): LocalShareDataSource

    @Binds
    abstract fun bindLocalShareKeyDataSource(impl: LocalShareKeyDataSourceImpl): LocalShareKeyDataSource

    @Binds
    abstract fun bindLocalEventDataSource(impl: LocalEventDataSourceImpl): LocalEventDataSource

    @Binds
    abstract fun bindLocalTelemetryDataSource(impl: LocalTelemetryDataSourceImpl): LocalTelemetryDataSource

    @Binds
    abstract fun bindLocalSearchEntryDataSource(impl: LocalSearchEntryDataSourceImpl): LocalSearchEntryDataSource

    @Binds
    abstract fun bindLocalPlanLimitsDataSource(impl: LocalPlanDataSourceImpl): LocalPlanDataSource

    @Binds
    abstract fun bindLocalDataMigrationDataSource(impl: LocalDataMigrationDataSourceImpl): LocalDataMigrationDataSource

    @Binds
    abstract fun bindLocalInviteDataSource(impl: LocalInviteDataSourceImpl): LocalInviteDataSource

    @Binds
    abstract fun bindLocalUserAccessDataDataSource(
        impl: LocalUserAccessDataDataSourceImpl
    ): LocalUserAccessDataDataSource

    @Binds
    abstract fun bindLocalOrganizationSettingsDataSource(
        impl: LocalOrganizationSettingsDataSourceImpl
    ): LocalOrganizationSettingsDataSource

    @[Binds Singleton]
    abstract fun bindLocalSentinelDataSource(impl: LocalSentinelDataSourceImpl): LocalSentinelDataSource

}
