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

package proton.android.pass.data.impl.network

import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.isRetryable
import proton.android.pass.domain.UserId
import javax.inject.Inject
import kotlin.reflect.KClass

class CoreRemoteApiGateway @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteApiGateway {

    override suspend fun <Api : BaseRetrofitApi> get(apiClass: KClass<Api>, userId: UserId?): RemoteApiCaller<Api> {
        val sessionId = userId?.let { apiProvider.sessionProvider.getSessionId(it) }
        return CoreRemoteApiCaller(apiProvider.get(apiClass, sessionId))
    }
}

private class CoreRemoteApiCaller<Api : BaseRetrofitApi>(
    private val manager: ApiManager<out Api>
) : RemoteApiCaller<Api> {

    override suspend fun <T> invoke(block: suspend Api.() -> T): RemoteResult<T> =
        manager.invoke(block).toRemoteResult()
}

internal fun <T> ApiResult<T>.toRemoteResult(): RemoteResult<T> = when (this) {
    is ApiResult.Success -> RemoteResult.Success(value)
    is ApiResult.Error.Http -> RemoteResult.Error.Http(
        status = httpCode,
        protonCode = proton?.code,
        message = proton?.error ?: message,
        cause = cause,
        isRetryable = isRetryable()
    )
    is ApiResult.Error.NoInternet -> RemoteResult.Error.NoInternet(cause, isRetryable())
    is ApiResult.Error -> RemoteResult.Error.Failure(cause, isRetryable())
}
