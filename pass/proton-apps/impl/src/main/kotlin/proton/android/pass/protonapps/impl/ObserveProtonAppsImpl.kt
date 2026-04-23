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

package proton.android.pass.protonapps.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.protonapps.api.AppInstalledChecker
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType
import proton.android.pass.protonapps.api.usecases.ObserveProtonApps
import javax.inject.Inject

internal class ObserveProtonAppsImpl @Inject constructor(
    private val appInstalledChecker: AppInstalledChecker,
    private val appConfig: AppConfig
) : ObserveProtonApps {

    override fun invoke(): Flow<List<ProtonApp>> = flow {
        val apps = ProtonAppType.entries
            .filter { type ->
                if (appConfig.flavor is BuildFlavor.Fdroid) type.fdroidFallbackUrl != null
                else true
            }
            .sortedBy(ProtonAppType::position)
            .map { type -> ProtonApp(type = type, isInstalled = appInstalledChecker.check(type.id)) }
        emit(apps)
    }
}
