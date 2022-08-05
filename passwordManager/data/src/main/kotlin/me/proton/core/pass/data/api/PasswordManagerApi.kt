package me.proton.core.pass.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.pass.data.crypto.CreateItemRequest
import me.proton.core.pass.data.crypto.CreateVaultRequest
import me.proton.core.pass.data.crypto.UpdateItemRequest
import me.proton.core.pass.data.responses.*
import retrofit2.http.*

internal const val PREFIX = "pass/v1"

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

    @PUT("$PREFIX/share/{shareId}/item/{itemId}")
    suspend fun updateItem(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemRequest
    ): CreateItemResponse

    @DELETE("$PREFIX/share/{shareId}/item/{itemId}")
    suspend fun deleteItem(@Path("shareId") shareId: String, @Path("itemId") itemId: String)

    // KeyPacket
    @GET("$PREFIX/share/{shareId}/item/{itemId}/keypacket")
    suspend fun getLatestKeyPacket(@Path("shareId") shareId: String, @Path("itemId") itemId: String): GetKeyPacketResponse
}
