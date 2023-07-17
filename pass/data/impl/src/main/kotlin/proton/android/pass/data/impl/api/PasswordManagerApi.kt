/*
 * Copyright (c) 2023 Proton AG
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

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.MigrateItemRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.TelemetryRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.responses.AliasDetailsResponse
import proton.android.pass.data.impl.responses.CreateItemAliasResponse
import proton.android.pass.data.impl.responses.CreateItemResponse
import proton.android.pass.data.impl.responses.CreateVaultResponse
import proton.android.pass.data.impl.responses.DeleteVaultResponse
import proton.android.pass.data.impl.responses.FeatureFlagsResponse
import proton.android.pass.data.impl.responses.GetAliasOptionsResponse
import proton.android.pass.data.impl.responses.GetEventsResponse
import proton.android.pass.data.impl.responses.GetItemLatestKeyResponse
import proton.android.pass.data.impl.responses.GetItemsResponse
import proton.android.pass.data.impl.responses.GetShareKeysResponse
import proton.android.pass.data.impl.responses.GetShareResponse
import proton.android.pass.data.impl.responses.GetSharesResponse
import proton.android.pass.data.impl.responses.LastEventIdResponse
import proton.android.pass.data.impl.responses.MigrateItemsResponse
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.responses.UpdateLastUsedTimeResponse
import proton.android.pass.data.impl.responses.UserAccessResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PREFIX = "pass/v1"

@Suppress("TooManyFunctions", "ComplexInterface")
interface PasswordManagerApi : BaseRetrofitApi {
    @POST("$PREFIX/vault")
    suspend fun createVault(@Body request: CreateVaultRequest): CreateVaultResponse

    @PUT("$PREFIX/vault/{shareId}")
    suspend fun updateVault(
        @Path("shareId") shareId: String,
        @Body request: UpdateVaultRequest
    ): CreateVaultResponse

    @DELETE("$PREFIX/vault/{shareId}")
    suspend fun deleteVault(@Path("shareId") shareId: String): DeleteVaultResponse

    @GET("$PREFIX/share")
    suspend fun getShares(): GetSharesResponse

    @GET("$PREFIX/share/{shareId}")
    suspend fun getShare(@Path("shareId") shareId: String): GetShareResponse

    @PUT("$PREFIX/vault/{shareId}/primary")
    suspend fun markAsPrimary(@Path("shareId") shareId: String)

    // Share Keys
    @GET("$PREFIX/share/{shareId}/key")
    suspend fun getShareKeys(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetShareKeysResponse

    // Item
    @GET("$PREFIX/share/{shareId}/item")
    suspend fun getItems(
        @Path("shareId") shareId: String,
        @Query("Since") sinceToken: String?,
        @Query("PageSize") pageSize: Int
    ): GetItemsResponse

    @POST("$PREFIX/share/{shareId}/item")
    suspend fun createItem(
        @Path("shareId") shareId: String,
        @Body request: CreateItemRequest
    ): CreateItemResponse

    @POST("$PREFIX/share/{shareId}/alias/custom")
    suspend fun createAlias(
        @Path("shareId") shareId: String,
        @Body request: CreateAliasRequest
    ): CreateItemResponse

    @POST("$PREFIX/share/{shareId}/item/with_alias")
    suspend fun createItemAndAlias(
        @Path("shareId") shareId: String,
        @Body request: CreateItemAliasRequest
    ): CreateItemAliasResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}")
    suspend fun updateItem(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest
    ): CreateItemResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/lastuse")
    suspend fun updateLastUsedTime(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateLastUsedTimeRequest
    ): UpdateLastUsedTimeResponse

    @POST("$PREFIX/share/{shareId}/item/trash")
    suspend fun trashItems(
        @Path("shareId") shareId: String,
        @Body request: TrashItemsRequest
    ): TrashItemsResponse

    @HTTP(method = "DELETE", path = "$PREFIX/share/{shareId}/item", hasBody = true)
    suspend fun deleteItems(@Path("shareId") shareId: String, @Body request: TrashItemsRequest)

    @POST("$PREFIX/share/{shareId}/item/untrash")
    suspend fun untrashItems(
        @Path("shareId") shareId: String,
        @Body request: TrashItemsRequest
    ): TrashItemsResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/share")
    suspend fun migrateItem(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: MigrateItemRequest
    ): CreateItemResponse

    @PUT("$PREFIX/share/{shareId}/item/share")
    suspend fun migrateItems(
        @Path("shareId") shareId: String,
        @Body request: MigrateItemsRequest
    ): MigrateItemsResponse

    // ItemKey
    @GET("$PREFIX/share/{shareId}/item/{itemId}/key/latest")
    suspend fun getItemLatestKey(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetItemLatestKeyResponse

    // Alias
    @GET("$PREFIX/share/{shareId}/alias/options")
    suspend fun getAliasOptions(@Path("shareId") shareId: String): GetAliasOptionsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}")
    suspend fun getAliasDetails(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): AliasDetailsResponse

    @POST("$PREFIX/share/{shareId}/alias/{itemId}/mailbox")
    suspend fun updateAliasMailboxes(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateAliasMailboxesRequest
    ): AliasDetailsResponse

    // Events
    @GET("$PREFIX/share/{shareId}/event")
    suspend fun getLastEventId(
        @Path("shareId") shareId: String
    ): LastEventIdResponse

    @GET("$PREFIX/share/{shareId}/event/{lastEventId}")
    suspend fun getEvents(
        @Path("shareId") shareId: String,
        @Path("lastEventId") lastEventId: String
    ): GetEventsResponse

    // Favicon
    @GET("core/v4/images/logo")
    suspend fun getFavicon(
        @Query("Domain") domain: String,
        @Query("Size") size: Int = 32,
        @Query("Mode") mode: String = "light",
        @Query("MaxScaleUpFactor") maxUpscaleFactor: Int = 4
    ): retrofit2.Response<okhttp3.ResponseBody>

    // User access
    @GET("$PREFIX/user/access")
    suspend fun userAccess(): UserAccessResponse

    // Telemetry
    @POST("/data/v1/stats/multiple")
    suspend fun sendTelemetry(@Body request: TelemetryRequest): retrofit2.Response<okhttp3.ResponseBody>

    // Feature flags
    @GET("/feature/v2/frontend")
    suspend fun getFeatureFlags(): FeatureFlagsResponse
}
