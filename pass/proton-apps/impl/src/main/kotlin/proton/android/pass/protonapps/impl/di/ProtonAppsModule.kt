/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.protonapps.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.protonapps.api.AppInstalledChecker
import proton.android.pass.protonapps.api.usecases.ObserveProtonApps
import proton.android.pass.protonapps.api.usecases.OpenProtonApp
import proton.android.pass.protonapps.impl.AppInstalledCheckerImpl
import proton.android.pass.protonapps.impl.ObserveProtonAppsImpl
import proton.android.pass.protonapps.impl.OpenProtonAppImpl

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ProtonAppsModule {

    @Binds
    abstract fun bindAppInstalledChecker(impl: AppInstalledCheckerImpl): AppInstalledChecker

    @Binds
    abstract fun bindObserveProtonApps(impl: ObserveProtonAppsImpl): ObserveProtonApps

    @Binds
    abstract fun bindOpenProtonApp(impl: OpenProtonAppImpl): OpenProtonApp
}
