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

package proton.android.pass.data.impl.fakes

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import proton.android.pass.data.impl.network.RemoteApiCaller
import proton.android.pass.data.impl.network.RemoteApiGateway
import proton.android.pass.data.impl.network.RemoteResult
import proton.android.pass.domain.UserId
import kotlin.reflect.KClass

class FakeRemoteApiGateway : RemoteApiGateway {

    data class Request(val apiClass: KClass<*>, val userId: UserId?)

    private val apis = mutableMapOf<KClass<*>, BaseRetrofitApi>()
    private val recordedRequests = mutableListOf<Request>()

    var error: RemoteResult.Error? = null

    val requests: List<Request> get() = recordedRequests

    fun <Api : BaseRetrofitApi> setApi(apiClass: KClass<Api>, api: Api) {
        apis[apiClass] = api
    }

    override suspend fun <Api : BaseRetrofitApi> get(apiClass: KClass<Api>, userId: UserId?): RemoteApiCaller<Api> {
        recordedRequests.add(Request(apiClass, userId))
        return FakeRemoteApiCaller(apiClass)
    }

    private inner class FakeRemoteApiCaller<Api : BaseRetrofitApi>(
        private val apiClass: KClass<Api>
    ) : RemoteApiCaller<Api> {

        override suspend fun <T> invoke(block: suspend Api.() -> T): RemoteResult<T> {
            error?.let { return it }
            val api = checkNotNull(apiClass.java.cast(apis[apiClass])) {
                "No fake api set for ${apiClass.simpleName}"
            }
            return RemoteResult.Success(api.block())
        }
    }
}
