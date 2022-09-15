package me.proton.core.pass.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.pass.data.requests.CreateAliasRequest
import me.proton.core.pass.data.requests.CreateItemRequest
import me.proton.core.pass.data.requests.CreateVaultRequest
import me.proton.core.pass.data.requests.TrashItemsRequest
import me.proton.core.pass.data.requests.UpdateItemRequest
import me.proton.core.pass.data.responses.AliasDetailsResponse
import me.proton.core.pass.data.responses.CreateItemResponse
import me.proton.core.pass.data.responses.CreateVaultResponse
import me.proton.core.pass.data.responses.GetAliasOptionsResponse
import me.proton.core.pass.data.responses.GetItemsResponse
import me.proton.core.pass.data.responses.GetKeyPacketResponse
import me.proton.core.pass.data.responses.GetShareResponse
import me.proton.core.pass.data.responses.GetSharesResponse
import me.proton.core.pass.data.responses.GetVaultKeysResponse
import me.proton.core.pass.data.responses.TrashItemsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PREFIX = "pass/v1"

@Suppress("TooManyFunctions")
interface PasswordManagerApi : BaseRetrofitApi {
    @POST("$PREFIX/vault")
    suspend fun createVault(@Body request: CreateVaultRequest): CreateVaultResponse

    @GET("$PREFIX/share")
    suspend fun getShares(): GetSharesResponse

    @GET("$PREFIX/share/{shareId}")
    suspend fun getShare(@Path("shareId") shareId: String): GetShareResponse

    // Vault Keys
    @GET("$PREFIX/share/{shareId}/key/vault")
    suspend fun getVaultKeys(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetVaultKeysResponse

    // Item
    @GET("$PREFIX/share/{shareId}/item")
    suspend fun getItems(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
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

    @PUT("$PREFIX/share/{shareId}/item/{itemId}")
    suspend fun updateItem(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest
    ): CreateItemResponse

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

    // KeyPacket
    @GET("$PREFIX/share/{shareId}/item/{itemId}/keypacket")
    suspend fun getLatestKeyPacket(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetKeyPacketResponse

    // Alias
    @GET("$PREFIX/share/{shareId}/alias/options")
    suspend fun getAliasOptions(@Path("shareId") shareId: String): GetAliasOptionsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}")
    suspend fun getAliasDetails(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): AliasDetailsResponse
}
