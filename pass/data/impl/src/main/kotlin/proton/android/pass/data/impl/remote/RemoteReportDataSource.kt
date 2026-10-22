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


package proton.android.pass.data.impl.remote

import okhttp3.RequestBody
import proton.android.pass.data.impl.core.api.CoreApi
import proton.android.pass.data.impl.network.RemoteApiGateway
import proton.android.pass.data.impl.network.get
import proton.android.pass.data.impl.network.valueOrThrow
import javax.inject.Inject

interface RemoteReportDataSource {
    suspend fun sendBugReport(body: RequestBody)
}

class RemoteReportDataSourceImpl @Inject constructor(
    private val apiProvider: RemoteApiGateway
) : RemoteReportDataSource {
    override suspend fun sendBugReport(body: RequestBody) {
        apiProvider.get<CoreApi>().invoke {
            sendBugReport(body)
        }.valueOrThrow
    }
}
