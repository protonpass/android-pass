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

package proton.android.pass.data.impl.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Streaming

interface CompromisedPasswordApi {

    @GET("split/sha1/last_change")
    suspend fun getLastChange(): Response<ResponseBody>

    @Streaming
    @GET("split/sha1/{p0}/{p1}/{p2}/{prefix}.gz")
    suspend fun getCompromisedSuffixes(
        @Path("p0") p0: String,
        @Path("p1") p1: String,
        @Path("p2") p2: String,
        @Path("prefix") prefix: String,
        @Header("If-None-Match") ifNoneMatch: String? = null,
        @Header("Add-Padding") addPadding: String = "true"
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://credential-check.protonweb.com/"
    }
}
