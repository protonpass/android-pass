/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.account.fakes.telemetry

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.telemetry.domain.TelemetryWorkerManager
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesTelemetryAccountModule {

    @Binds
    abstract fun bindTelemetryRepository(impl: FakeTelemetryRepository): TelemetryRepository

    @Binds
    abstract fun bindIsTelemetryEnabled(impl: FakeIsTelemetryEnabled): IsTelemetryEnabled

    @Binds
    abstract fun bindTelemetryWorkerManager(impl: FakeTelemetryWorkerManager): TelemetryWorkerManager

}
