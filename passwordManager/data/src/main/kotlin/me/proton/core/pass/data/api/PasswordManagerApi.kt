package me.proton.core.pass.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.pass.data.crypto.CreateItemRequest
import me.proton.core.pass.data.crypto.CreateVaultRequest
import me.proton.core.pass.data.responses.CreateItemResponse
import me.proton.core.pass.data.responses.CreateVaultResponse
import me.proton.core.pass.data.responses.GetItemsResponse
import me.proton.core.pass.data.responses.GetShareResponse
import me.proton.core.pass.data.responses.GetSharesResponse
import me.proton.core.pass.data.responses.GetVaultKeysResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PasswordManagerApi : BaseRetrofitApi {
    @POST("pass/v1/vault")
    suspend fun createVault(@Body request: CreateVaultRequest): CreateVaultResponse

    @GET("pass/v1/share")
    suspend fun getShares(): GetSharesResponse

    @GET("pass/v1/share/{shareId}")
    suspend fun getShare(@Path("shareId") shareId: String): GetShareResponse

    // Vault Keys
    @GET("pass/v1/share/{shareId}/key/vault")
    suspend fun getVaultKeys(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetVaultKeysResponse

    // Item
    @GET("pass/v1/share/{shareId}/item")
    suspend fun getItems(
        @Path("shareId") shareId: String,
        @Query("Page") page: Int,
        @Query("PageSize") pageSize: Int
    ): GetItemsResponse

    @POST("pass/v1/share/{shareId}/item")
    suspend fun createItem(
        @Path("shareId") shareId: String,
        @Body request: CreateItemRequest
    ): CreateItemResponse

    @DELETE("pass/v1/share/{shareId}/item/{itemId}")
    suspend fun deleteItem(@Path("shareId") shareId: String, @Path("itemId") itemId: String)
}
