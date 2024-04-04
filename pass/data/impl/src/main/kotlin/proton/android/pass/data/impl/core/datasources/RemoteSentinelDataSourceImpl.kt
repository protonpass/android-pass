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

package proton.android.pass.data.impl.core.datasources

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.network.data.ApiProvider
import me.proton.core.util.kotlin.toBoolean
import proton.android.pass.data.api.core.datasources.RemoteSentinelDataSource
import proton.android.pass.data.impl.core.api.CoreApi
import javax.inject.Inject

class RemoteSentinelDataSourceImpl @Inject constructor(
    private val api: ApiProvider,
    private val accountManager: AccountManager
) : RemoteSentinelDataSource {

    override suspend fun disableSentinel() {
        getCoreApi()
            .invoke { disableHighSecuritySetting() }
            .valueOrThrow
    }

    override suspend fun enableSentinel() {
        getCoreApi()
            .invoke { enableHighSecuritySetting() }
            .valueOrThrow
    }

    override suspend fun isSentinelEnabled(): Boolean = getCoreApi()
        .invoke { getSettings() }
        .valueOrThrow
        .userSettings
        .highSecurity
        .value
        .toBoolean()

    private suspend fun getCoreApi() = accountManager.getPrimaryUserId()
        .first()
        .let { userId -> api.get<CoreApi>(userId) }

}
