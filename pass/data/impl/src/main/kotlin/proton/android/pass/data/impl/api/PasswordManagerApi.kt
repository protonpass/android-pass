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
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import proton.android.pass.data.impl.requests.AcceptInviteRequest
import proton.android.pass.data.impl.requests.BatchHideUnhideShareRequest
import proton.android.pass.data.impl.requests.BreachAddEmailRequest
import proton.android.pass.data.impl.requests.BreachVerifyEmailRequest
import proton.android.pass.data.impl.requests.ChangeAliasStatusRequest
import proton.android.pass.data.impl.requests.ChangeNotificationStatusRequest
import proton.android.pass.data.impl.requests.CheckAddressesCanBeInvitedRequest
import proton.android.pass.data.impl.requests.ConfirmInviteRequest
import proton.android.pass.data.impl.requests.CreateAliasRequest
import proton.android.pass.data.impl.requests.CreateInviteRequest
import proton.android.pass.data.impl.requests.CreateInvitesRequest
import proton.android.pass.data.impl.requests.CreateItemAliasRequest
import proton.android.pass.data.impl.requests.CreateItemRequest
import proton.android.pass.data.impl.requests.CreateNewUserInviteRequest
import proton.android.pass.data.impl.requests.CreateNewUserInvitesRequest
import proton.android.pass.data.impl.requests.CreateSecureLinkRequest
import proton.android.pass.data.impl.requests.CreateVaultRequest
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.data.impl.requests.ItemReadRequest
import proton.android.pass.data.impl.requests.MigrateItemsRequest
import proton.android.pass.data.impl.requests.SendUserMonitorCredentialsRequest
import proton.android.pass.data.impl.requests.SetupExtraPasswordRequest
import proton.android.pass.data.impl.requests.SimpleLoginChangeMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreateAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesRequest
import proton.android.pass.data.impl.requests.SimpleLoginDeleteAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDefaultMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDomainRequest
import proton.android.pass.data.impl.requests.SimpleLoginVerifyAliasMailboxRequest
import proton.android.pass.data.impl.requests.TelemetryRequest
import proton.android.pass.data.impl.requests.TransferVaultOwnershipRequest
import proton.android.pass.data.impl.requests.TrashItemsRequest
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.requests.UpdateItemFlagsRequest
import proton.android.pass.data.impl.requests.UpdateItemRequest
import proton.android.pass.data.impl.requests.UpdateLastUsedTimeRequest
import proton.android.pass.data.impl.requests.UpdateMemberShareRequest
import proton.android.pass.data.impl.requests.UpdateVaultRequest
import proton.android.pass.data.impl.requests.alias.UpdateAliasNameRequest
import proton.android.pass.data.impl.requests.alias.UpdateAliasNoteRequest
import proton.android.pass.data.impl.requests.aliascontacts.CreateAliasContactRequest
import proton.android.pass.data.impl.requests.aliascontacts.UpdateBlockedAliasContactRequest
import proton.android.pass.data.impl.requests.attachments.CreatePendingFileRequest
import proton.android.pass.data.impl.requests.attachments.LinkPendingFilesRequest
import proton.android.pass.data.impl.requests.attachments.RestoreOldFileRequest
import proton.android.pass.data.impl.requests.attachments.UpdateFileMetadataRequest
import proton.android.pass.data.impl.requests.attachments.UpdatePendingFileRequest
import proton.android.pass.data.impl.responses.AliasDetailsResponse
import proton.android.pass.data.impl.responses.BatchHideUnhideSharesResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailsResponse
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.data.impl.responses.ChangeNotificationStateResponse
import proton.android.pass.data.impl.responses.CheckAddressesCanBeInvitedResponse
import proton.android.pass.data.impl.responses.CodeOnlyResponse
import proton.android.pass.data.impl.responses.CreateItemAliasResponse
import proton.android.pass.data.impl.responses.CreateSecureLinkResponse
import proton.android.pass.data.impl.responses.CreateVaultResponse
import proton.android.pass.data.impl.responses.DeleteVaultResponse
import proton.android.pass.data.impl.responses.ExtraPasswordGetSrpDataResponse
import proton.android.pass.data.impl.responses.GetAliasOptionsResponse
import proton.android.pass.data.impl.responses.GetAllKeysByAddressResponse
import proton.android.pass.data.impl.responses.GetAllSecureLinksResponse
import proton.android.pass.data.impl.responses.GetEventsResponse
import proton.android.pass.data.impl.responses.GetItemLatestKeyResponse
import proton.android.pass.data.impl.responses.GetItemRevisionsResponse
import proton.android.pass.data.impl.responses.GetItemsResponse
import proton.android.pass.data.impl.responses.GetShareKeysResponse
import proton.android.pass.data.impl.responses.GetShareMembersResponse
import proton.android.pass.data.impl.responses.GetSharePendingInvitesResponse
import proton.android.pass.data.impl.responses.GetShareResponse
import proton.android.pass.data.impl.responses.GetSharesResponse
import proton.android.pass.data.impl.responses.GetUserNotificationsResponse
import proton.android.pass.data.impl.responses.InviteRecommendationsResponse
import proton.android.pass.data.impl.responses.ItemRevisionResponse
import proton.android.pass.data.impl.responses.LastEventIdResponse
import proton.android.pass.data.impl.responses.MigrateItemsResponse
import proton.android.pass.data.impl.responses.OrganizationGetResponse
import proton.android.pass.data.impl.responses.PendingInvitesResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasDomainsResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxesResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasSettingsResponse
import proton.android.pass.data.impl.responses.SimpleLoginPendingAliasesResponse
import proton.android.pass.data.impl.responses.SimpleLoginSyncStatusResponse
import proton.android.pass.data.impl.responses.TrashItemsResponse
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateRequest
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateResponse
import proton.android.pass.data.impl.responses.UpdateLastUsedTimeResponse
import proton.android.pass.data.impl.responses.UpdateMonitorAddressStateRequest
import proton.android.pass.data.impl.responses.UserAccessResponse
import proton.android.pass.data.impl.responses.UserSyncEventsResponse
import proton.android.pass.data.impl.responses.aliascontacts.CreateAliasContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.GetAliasContactResponse
import proton.android.pass.data.impl.responses.aliascontacts.GetAliasContactsResponse
import proton.android.pass.data.impl.responses.aliascontacts.UpdateBlockedAliasContactResponse
import proton.android.pass.data.impl.responses.attachments.PendingFileResponse
import proton.android.pass.data.impl.responses.attachments.RestoreOldFileResponse
import proton.android.pass.data.impl.responses.attachments.RetrieveFilesResponse
import proton.android.pass.data.impl.responses.attachments.UpdateFileMetadataResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

internal const val PREFIX = "pass/v1"

@Suppress("TooManyFunctions", "ComplexInterface")
interface PasswordManagerApi : BaseRetrofitApi {
    @POST("$PREFIX/vault")
    suspend fun createVault(@Body request: CreateVaultRequest): CreateVaultResponse

    @PUT("$PREFIX/vault/{shareId}")
    suspend fun updateVault(@Path("shareId") shareId: String, @Body request: UpdateVaultRequest): CreateVaultResponse

    @DELETE("$PREFIX/vault/{shareId}")
    suspend fun deleteVault(@Path("shareId") shareId: String): DeleteVaultResponse

    @GET("$PREFIX/share")
    suspend fun getShares(@Query("EventToken") eventToken: String?): GetSharesResponse

    @GET("$PREFIX/share/{shareId}")
    suspend fun getShare(@Path("shareId") shareId: String, @Query("EventToken") eventToken: String?): GetShareResponse

    @DELETE("$PREFIX/share/{shareId}")
    suspend fun leaveShare(@Path("shareId") shareId: String): CodeOnlyResponse

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
        @Query("PageSize") pageSize: Int,
        @Query("EventToken") eventToken: String?
    ): GetItemsResponse

    @POST("$PREFIX/share/{shareId}/item")
    suspend fun createItem(@Path("shareId") shareId: String, @Body request: CreateItemRequest): ItemRevisionResponse

    @POST("$PREFIX/share/{shareId}/alias/custom")
    suspend fun createAlias(@Path("shareId") shareId: String, @Body request: CreateAliasRequest): ItemRevisionResponse

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
    ): ItemRevisionResponse

    @DELETE("$PREFIX/share/{shareId}/item/{itemId}/revisions")
    suspend fun deleteItemRevisions(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): ItemRevisionResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/flags")
    suspend fun updateItemFlags(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateItemFlagsRequest
    ): ItemRevisionResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/lastuse")
    suspend fun updateLastUsedTime(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateLastUsedTimeRequest
    ): UpdateLastUsedTimeResponse

    @POST("$PREFIX/share/{shareId}/item/{itemId}/pin")
    suspend fun pinItem(@Path("shareId") shareId: String, @Path("itemId") itemId: String): ItemRevisionResponse

    @DELETE("$PREFIX/share/{shareId}/item/{itemId}/pin")
    suspend fun unpinItem(@Path("shareId") shareId: String, @Path("itemId") itemId: String): ItemRevisionResponse

    @POST("$PREFIX/share/{shareId}/item/trash")
    suspend fun trashItems(@Path("shareId") shareId: String, @Body request: TrashItemsRequest): TrashItemsResponse

    @HTTP(method = "DELETE", path = "$PREFIX/share/{shareId}/item", hasBody = true)
    suspend fun deleteItems(@Path("shareId") shareId: String, @Body request: TrashItemsRequest)

    @POST("$PREFIX/share/{shareId}/item/untrash")
    suspend fun untrashItems(@Path("shareId") shareId: String, @Body request: TrashItemsRequest): TrashItemsResponse

    @PUT("$PREFIX/share/{shareId}/item/share")
    suspend fun migrateItems(@Path("shareId") shareId: String, @Body request: MigrateItemsRequest): MigrateItemsResponse

    // ItemKey
    @GET("$PREFIX/share/{shareId}/item/{itemId}/key/latest")
    suspend fun getItemLatestKey(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetItemLatestKeyResponse

    @GET("$PREFIX/share/{shareId}/item/{itemId}/revision")
    suspend fun getItemRevision(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetItemRevisionsResponse

    // Alias
    @GET("$PREFIX/share/{shareId}/alias/options")
    suspend fun getAliasOptions(@Path("shareId") shareId: String): GetAliasOptionsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}")
    suspend fun getAliasDetails(@Path("shareId") shareId: String, @Path("itemId") itemId: String): AliasDetailsResponse

    @POST("$PREFIX/share/{shareId}/alias/{itemId}/mailbox")
    suspend fun updateAliasMailboxes(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateAliasMailboxesRequest
    ): AliasDetailsResponse

    @PUT("$PREFIX/share/{shareId}/alias/{itemId}/status")
    suspend fun changeAliasStatus(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: ChangeAliasStatusRequest
    ): CodeOnlyResponse

    @PUT("$PREFIX/share/{shareId}/alias/{itemId}/name")
    suspend fun updateAliasName(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateAliasNameRequest
    ): CodeOnlyResponse

    @PUT("$PREFIX/share/{shareId}/alias/{itemId}/note")
    suspend fun updateAliasNote(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateAliasNoteRequest
    ): CodeOnlyResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}/contact")
    suspend fun getAliasContacts(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Query("Since") since: Int?
    ): GetAliasContactsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}/contact/{contactId}")
    suspend fun getAliasContact(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("contactId") contactId: Int
    ): GetAliasContactResponse

    @POST("$PREFIX/share/{shareId}/alias/{itemId}/contact")
    suspend fun createAliasContact(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: CreateAliasContactRequest
    ): CreateAliasContactResponse

    @DELETE("$PREFIX/share/{shareId}/alias/{itemId}/contact/{contactId}")
    suspend fun deleteAliasContact(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("contactId") contactId: Int
    ): CodeOnlyResponse

    @PUT("$PREFIX/share/{shareId}/alias/{itemId}/contact/{contactId}/blocked")
    suspend fun updateBlockedAliasContact(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("contactId") contactId: Int,
        @Body request: UpdateBlockedAliasContactRequest
    ): UpdateBlockedAliasContactResponse

    // Events
    @GET("$PREFIX/share/{shareId}/event")
    suspend fun getLastEventId(@Path("shareId") shareId: String): LastEventIdResponse

    @GET("$PREFIX/share/{shareId}/event/{lastEventId}")
    suspend fun getEvents(@Path("shareId") shareId: String, @Path("lastEventId") lastEventId: String): GetEventsResponse

    // Favicon
    @GET("core/v4/images/logo")
    suspend fun getFavicon(
        @Query("Domain") domain: String,
        @Query("Size") size: Int = 32,
        @Query("Mode") mode: String = "light",
        @Query("MaxScaleUpFactor") maxUpscaleFactor: Int = 4
    ): retrofit2.Response<ResponseBody>

    // User access
    @GET("$PREFIX/user/access")
    suspend fun userAccess(): UserAccessResponse

    @GET("$PREFIX/user/sync_event")
    suspend fun getLatestUserEventId(): LastEventIdResponse

    @GET("$PREFIX/user/sync_event/{lastEventId}")
    suspend fun getUserSyncEvents(@Path("lastEventId") lastEventId: String): UserSyncEventsResponse

    // Telemetry
    @POST("/data/v1/stats/multiple")
    suspend fun sendTelemetry(@Body request: TelemetryRequest): retrofit2.Response<ResponseBody>

    @PUT("$PREFIX/share/{shareId}/item/read")
    suspend fun sendItemReadEvent(@Path("shareId") shareId: String, @Body request: ItemReadRequest): CodeOnlyResponse

    // Sharing
    @POST("$PREFIX/share/{shareId}/invite")
    suspend fun inviteUser(@Path("shareId") shareId: String, @Body request: CreateInviteRequest): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/invite/batch")
    suspend fun inviteUsers(@Path("shareId") shareId: String, @Body request: CreateInvitesRequest): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/invite/new_user")
    suspend fun inviteNewUser(
        @Path("shareId") shareId: String,
        @Body request: CreateNewUserInviteRequest
    ): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/invite/new_user/batch")
    suspend fun inviteNewUsers(
        @Path("shareId") shareId: String,
        @Body request: CreateNewUserInvitesRequest
    ): CodeOnlyResponse

    @GET("$PREFIX/invite")
    suspend fun fetchInvites(): PendingInvitesResponse

    @POST("$PREFIX/invite/{inviteId}")
    suspend fun acceptInvite(@Path("inviteId") inviteId: String, @Body request: AcceptInviteRequest): GetShareResponse

    @POST("$PREFIX/share/{shareId}/invite/new_user/{inviteId}/keys")
    suspend fun confirmInvite(
        @Path("shareId") shareId: String,
        @Path("inviteId") inviteId: String,
        @Body request: ConfirmInviteRequest
    ): CodeOnlyResponse

    @DELETE("$PREFIX/invite/{inviteId}")
    suspend fun rejectInvite(@Path("inviteId") inviteId: String): CodeOnlyResponse

    @GET("$PREFIX/share/{shareId}/invite/recommended_emails")
    suspend fun inviteRecommendations(
        @Path("shareId") shareId: String,
        @Query("PlanSince") lastToken: String?,
        @Query("StartsWith") startsWith: String?
    ): InviteRecommendationsResponse

    @GET("$PREFIX/share/{shareId}/user")
    suspend fun getShareMembers(@Path("shareId") shareId: String): GetShareMembersResponse

    @GET("$PREFIX/share/{shareId}/user/item/{itemId}")
    suspend fun getShareItemMembers(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): GetShareMembersResponse

    @GET("$PREFIX/share/{shareId}/invite")
    suspend fun getPendingInvitesForShare(@Path("shareId") shareId: String): GetSharePendingInvitesResponse

    @DELETE("$PREFIX/share/{shareId}/user/{memberShareId}")
    suspend fun deleteShareMember(
        @Path("shareId") shareId: String,
        @Path("memberShareId") memberShareId: String
    ): CodeOnlyResponse

    @PUT("$PREFIX/share/{shareId}/user/{memberShareId}")
    suspend fun updateShareMember(
        @Path("shareId") shareId: String,
        @Path("memberShareId") memberShareId: String,
        @Body request: UpdateMemberShareRequest
    ): CodeOnlyResponse

    @DELETE("$PREFIX/share/{shareId}/invite/{inviteId}")
    suspend fun deleteInvite(@Path("shareId") shareId: String, @Path("inviteId") inviteId: String): CodeOnlyResponse

    @DELETE("$PREFIX/share/{shareId}/invite/new_user/{inviteId}")
    suspend fun deleteNewUserInvite(
        @Path("shareId") shareId: String,
        @Path("inviteId") inviteId: String
    ): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/invite/{inviteId}/reminder")
    suspend fun sendInviteReminder(
        @Path("shareId") shareId: String,
        @Path("inviteId") inviteId: String
    ): CodeOnlyResponse

    @PUT("$PREFIX/vault/{shareId}/owner")
    suspend fun transferVaultOwnership(
        @Path("shareId") shareId: String,
        @Body request: TransferVaultOwnershipRequest
    ): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/invite/check_address")
    suspend fun checkAddressesCanBeInvited(
        @Path("shareId") shareId: String,
        @Body request: CheckAddressesCanBeInvitedRequest
    ): CheckAddressesCanBeInvitedResponse

    // Organization
    @GET("$PREFIX/organization")
    suspend fun getOrganization(): OrganizationGetResponse

    @POST("$PREFIX/organization/report/client_data")
    suspend fun sendUserMonitorCredentialsReport(@Body request: SendUserMonitorCredentialsRequest): CodeOnlyResponse

    // Breach
    @GET("$PREFIX/breach")
    suspend fun getAllBreaches(): BreachesResponse

    @GET("$PREFIX/breach/custom_email")
    suspend fun getBreachCustomEmails(): BreachCustomEmailsResponse

    @POST("$PREFIX/breach/custom_email")
    suspend fun addBreachEmailToMonitor(@Body request: BreachAddEmailRequest): BreachCustomEmailResponse

    @PUT("$PREFIX/breach/custom_email/{customEmailId}/verify")
    suspend fun verifyBreachEmail(
        @Path("customEmailId") emailId: String,
        @Body request: BreachVerifyEmailRequest
    ): CodeOnlyResponse

    @POST("$PREFIX/breach/custom_email/{customEmailId}/resend_verification")
    suspend fun resendVerificationCode(@Path("customEmailId") emailId: String): CodeOnlyResponse

    @DELETE("$PREFIX/breach/custom_email/{customEmailId}")
    suspend fun removeCustomEmail(@Path("customEmailId") emailId: String): CodeOnlyResponse

    @GET("$PREFIX/breach/address/{addressId}/breaches")
    suspend fun getBreachesForProtonEmail(@Path("addressId") addressId: String): BreachEmailsResponse

    @GET("$PREFIX/breach/custom_email/{customEmailId}/breaches")
    suspend fun getBreachesForCustomEmail(@Path("customEmailId") emailId: String): BreachEmailsResponse

    @GET("$PREFIX/share/{shareId}/alias/{itemId}/breaches")
    suspend fun getBreachesForAliasEmail(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): BreachEmailsResponse

    @POST("$PREFIX/breach/address/{addressId}/resolved")
    suspend fun markProtonEmailAsResolved(@Path("addressId") addressId: String): CodeOnlyResponse

    @PUT("$PREFIX/breach/custom_email/{customEmailId}/resolved")
    suspend fun markCustomEmailAsResolved(@Path("customEmailId") emailId: String): BreachCustomEmailResponse

    @POST("$PREFIX/share/{shareId}/alias/{itemId}/breaches/resolved")
    suspend fun markAliasEmailAsResolved(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String
    ): CodeOnlyResponse

    @PUT("$PREFIX/user/monitor")
    suspend fun updateGlobalMonitorState(
        @Body request: UpdateGlobalMonitorStateRequest
    ): UpdateGlobalMonitorStateResponse

    @PUT("$PREFIX/breach/address/{addressId}/monitor")
    suspend fun updateProtonAddressMonitorState(
        @Path("addressId") addressId: String,
        @Body request: UpdateMonitorAddressStateRequest
    ): CodeOnlyResponse

    // Public link
    @POST("$PREFIX/share/{shareId}/item/{itemId}/public_link")
    suspend fun generateSecureLink(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: CreateSecureLinkRequest
    ): CreateSecureLinkResponse

    @GET("$PREFIX/public_link")
    suspend fun getAllSecureLinks(): GetAllSecureLinksResponse

    @DELETE("$PREFIX/public_link/{secureLinkId}")
    suspend fun deleteSecureLink(@Path("secureLinkId") secureLinkId: String): CodeOnlyResponse

    @DELETE("$PREFIX/public_link/inactive")
    suspend fun deleteInactiveSecureLinks(): CodeOnlyResponse

    // Extra Password
    @POST("$PREFIX/user/srp")
    suspend fun setupExtraPassword(@Body request: SetupExtraPasswordRequest)

    @DELETE("$PREFIX/user/srp")
    suspend fun removeExtraPassword()

    @GET("$PREFIX/user/srp/info")
    suspend fun getSrpInfo(): ExtraPasswordGetSrpDataResponse

    @POST("$PREFIX/user/srp/auth")
    suspend fun sendSrpInfo(@Body extraPasswordSendSrpDataRequest: ExtraPasswordSendSrpDataRequest): CodeOnlyResponse

    // SimpleLogin sync
    @GET("$PREFIX/alias_sync/status")
    suspend fun getSimpleLoginSyncStatus(): SimpleLoginSyncStatusResponse

    @POST("$PREFIX/alias_sync/sync")
    suspend fun enableSimpleLoginSync(@Body request: SimpleLoginEnableSyncRequest): CodeOnlyResponse

    @GET("$PREFIX/user/alias/domain")
    suspend fun getSimpleLoginAliasDomains(): SimpleLoginAliasDomainsResponse

    @PUT("$PREFIX/user/alias/settings/default_alias_domain")
    suspend fun updateSimpleLoginAliasDomain(
        @Body request: SimpleLoginUpdateAliasDomainRequest
    ): SimpleLoginAliasSettingsResponse

    @GET("$PREFIX/user/alias/mailbox")
    suspend fun getSimpleLoginAliasMailboxes(): SimpleLoginAliasMailboxesResponse

    @POST("$PREFIX/user/alias/mailbox")
    suspend fun createSimpleLoginAliasMailbox(
        @Body request: SimpleLoginCreateAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    @POST("$PREFIX/user/alias/mailbox/{mailboxId}/verify")
    suspend fun verifySimpleLoginAliasMailbox(
        @Path("mailboxId") mailboxId: Long,
        @Body request: SimpleLoginVerifyAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    @GET("$PREFIX/user/alias/mailbox/{mailboxId}/verify")
    suspend fun resendSimpleLoginAliasMailboxVerifyCode(
        @Path("mailboxId") mailboxId: Long
    ): SimpleLoginAliasMailboxResponse

    @PUT("$PREFIX/user/alias/mailbox/{mailboxId}/email")
    suspend fun changeSimpleLoginAliasMailboxEmail(
        @Path("mailboxId") mailboxId: Long,
        @Body request: SimpleLoginChangeMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    @DELETE("$PREFIX/user/alias/mailbox/{mailboxId}/email")
    suspend fun cancelSimpleLoginAliasMailboxEmailChange(@Path("mailboxId") mailboxId: Long): CodeOnlyResponse

    @HTTP(method = "DELETE", path = "$PREFIX/user/alias/mailbox/{mailboxId}", hasBody = true)
    suspend fun deleteSimpleLoginAliasMailbox(
        @Path("mailboxId") mailboxId: Long,
        @Body request: SimpleLoginDeleteAliasMailboxRequest
    ): CodeOnlyResponse

    @PUT("$PREFIX/user/alias/settings/default_mailbox_id")
    suspend fun updateSimpleLoginAliasDefaultMailbox(
        @Body request: SimpleLoginUpdateAliasDefaultMailboxRequest
    ): SimpleLoginAliasSettingsResponse

    @GET("$PREFIX/user/alias/settings")
    suspend fun getSimpleLoginAliasSettings(): SimpleLoginAliasSettingsResponse

    @GET("$PREFIX/alias_sync/pending")
    suspend fun getSimpleLoginPendingAliases(): SimpleLoginPendingAliasesResponse

    @POST("$PREFIX/alias_sync/share/{shareId}/create")
    suspend fun createSimpleLoginPendingAliases(
        @Path("shareId") shareId: String,
        @Body request: SimpleLoginCreatePendingAliasesRequest
    ): GetItemsResponse

    // Notifications
    @GET("$PREFIX/notification")
    suspend fun fetchUserNotifications(@Query("Since") sinceToken: String?): GetUserNotificationsResponse

    @PUT("$PREFIX/notification/{notificationId}")
    suspend fun changeNotificationStatus(
        @Path("notificationId") notificationId: String,
        @Body request: ChangeNotificationStatusRequest
    ): ChangeNotificationStateResponse

    // Attachments
    @POST("$PREFIX/file")
    suspend fun createPendingFile(@Body request: CreatePendingFileRequest): PendingFileResponse

    @POST("$PREFIX/share/{shareId}/item/{itemId}/link_files")
    suspend fun linkPendingFiles(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Body request: LinkPendingFilesRequest
    ): CodeOnlyResponse

    @POST("$PREFIX/share/{shareId}/item/{itemId}/file/{fileId}/restore")
    suspend fun restoreOldFile(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("fileId") fileId: String,
        @Body request: RestoreOldFileRequest
    ): RestoreOldFileResponse

    @PUT("$PREFIX/file/{fileId}/metadata")
    suspend fun updatePendingFileMetadata(
        @Path("fileId") fileId: String,
        @Body request: UpdatePendingFileRequest
    ): PendingFileResponse

    @PUT("$PREFIX/share/{shareId}/item/{itemId}/file/{fileId}/metadata")
    suspend fun updateFileMetadata(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("fileId") fileId: String,
        @Body request: UpdateFileMetadataRequest
    ): UpdateFileMetadataResponse

    @GET("$PREFIX/share/{shareId}/item/{itemId}/files")
    suspend fun retrieveActiveFiles(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Query("Since") lastToken: String?
    ): RetrieveFilesResponse

    @GET("$PREFIX/share/{shareId}/item/{itemId}/revisions/files")
    suspend fun retrieveAllFilesForAllRevisions(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Query("Since") lastToken: String?
    ): RetrieveFilesResponse

    @Multipart
    @POST("$PREFIX/file/{fileId}/chunk")
    suspend fun uploadChunk(
        @Path("fileId") fileId: String,
        @Part("ChunkIndex") chunkIndex: RequestBody,
        @Part chunkData: MultipartBody.Part
    ): CodeOnlyResponse

    @GET("$PREFIX/share/{shareId}/item/{itemId}/file/{fileId}/chunk/{chunkId}")
    suspend fun downloadChunk(
        @Path("shareId") shareId: String,
        @Path("itemId") itemId: String,
        @Path("fileId") fileId: String,
        @Path("chunkId") chunkId: String
    ): retrofit2.Response<ResponseBody>

    // Core
    @GET("core/v4/keys/all")
    suspend fun getAllKeysByAddress(
        @Query("Email") email: String,
        @Query("InternalOnly") internalOnly: Int = 1
    ): GetAllKeysByAddressResponse

    @PUT("$PREFIX/share/hide")
    suspend fun changeShareVisibility(@Body request: BatchHideUnhideShareRequest): BatchHideUnhideSharesResponse
}
