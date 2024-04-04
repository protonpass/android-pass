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

package proton.android.pass.data.impl.core.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import proton.android.pass.data.impl.core.responses.CoreCodeResponse
import proton.android.pass.data.impl.core.responses.CoreSettingsResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

private const val ROOT_PATH = "core/v4"

interface CoreApi : BaseRetrofitApi {

    @GET("$ROOT_PATH/settings")
    suspend fun getSettings(): CoreSettingsResponse

    @POST("$ROOT_PATH/settings/highsecurity")
    suspend fun enableHighSecuritySetting(): CoreCodeResponse

    @DELETE("$ROOT_PATH/settings/highsecurity")
    suspend fun disableHighSecuritySetting(): CoreCodeResponse

}
