/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.telemetry.data.repository.TelemetryLocalDataSourceImpl
import me.proton.core.telemetry.data.repository.TelemetryRemoteDataSourceImpl
import me.proton.core.telemetry.data.repository.TelemetryRepositoryImpl
import me.proton.core.telemetry.data.worker.TelemetryWorkerManagerImpl
import me.proton.core.telemetry.domain.TelemetryWorkerManager
import me.proton.core.telemetry.domain.repository.TelemetryLocalDataSource
import me.proton.core.telemetry.domain.repository.TelemetryRemoteDataSource
import me.proton.core.telemetry.domain.repository.TelemetryRepository

@Module
@InstallIn(SingletonComponent::class)
public interface AccountTelemetryModule {

    @Binds
    public fun bindTelemetryRepository(impl: TelemetryRepositoryImpl): TelemetryRepository

    @Binds
    public fun bindTelemetryLocalDataSource(impl: TelemetryLocalDataSourceImpl): TelemetryLocalDataSource

    @Binds
    public fun bindTelemetryRemoteDataSource(impl: TelemetryRemoteDataSourceImpl): TelemetryRemoteDataSource

    @Binds
    public fun bindTelemetryWorkerManager(impl: TelemetryWorkerManagerImpl): TelemetryWorkerManager
}
