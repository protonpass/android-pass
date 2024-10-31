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

package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.errors.InvalidVerificationCodeError
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.BreachAddEmailRequest
import proton.android.pass.data.impl.requests.BreachVerifyEmailRequest
import proton.android.pass.data.impl.responses.BreachCustomEmailResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailsResponse
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateRequest
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateResponse
import proton.android.pass.data.impl.responses.UpdateMonitorAddressStateRequest
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.CustomEmailId
import javax.inject.Inject

interface RemoteBreachDataSource {

    suspend fun getAllBreaches(userId: UserId): BreachesResponse

    suspend fun getCustomEmails(userId: UserId): BreachCustomEmailsResponse

    suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmailResponse

    suspend fun verifyCustomEmail(
        userId: UserId,
        id: CustomEmailId,
        code: String
    )

    suspend fun getBreachesForProtonEmail(userId: UserId, id: AddressId): BreachEmailsResponse

    suspend fun getBreachesForCustomEmail(userId: UserId, id: CustomEmailId): BreachEmailsResponse

    suspend fun getBreachesForAliasEmail(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): BreachEmailsResponse

    suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId)
    suspend fun markAliasEmailAsResolved(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    )

    suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId): BreachCustomEmailResponse

    suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId)

    suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId)

    suspend fun updateGlobalProtonAddressMonitorState(
        userId: UserId,
        enabled: Boolean
    ): UpdateGlobalMonitorStateResponse

    suspend fun updateGlobalAliasAddressMonitorState(userId: UserId, enabled: Boolean): UpdateGlobalMonitorStateResponse

    suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        id: AddressId,
        enabled: Boolean
    )
}

class RemoteBreachDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteBreachDataSource {

    override suspend fun getAllBreaches(userId: UserId): BreachesResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getAllBreaches() }
        .valueOrThrow

    override suspend fun getCustomEmails(userId: UserId): BreachCustomEmailsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            getBreachCustomEmails()
        }
        .valueOrThrow


    override suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmailResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            addBreachEmailToMonitor(BreachAddEmailRequest(email))
        }
        .valueOrThrow

    override suspend fun verifyCustomEmail(
        userId: UserId,
        id: CustomEmailId,
        code: String
    ) {
        val response = apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                verifyBreachEmail(emailId = id.id, request = BreachVerifyEmailRequest(code))
            }

        when (response) {
            is ApiResult.Error.Http -> {
                val protonCode = response.proton?.code

                when (protonCode) {
                    INVALID_VALUE -> throw InvalidVerificationCodeError
                    NOT_ALLOWED -> throw CustomEmailDoesNotExistException()
                    else -> response.throwIfError()
                }

            }
            is ApiResult.Success -> {} // All good
            else -> { response.throwIfError() }
        }
    }

    override suspend fun getBreachesForProtonEmail(userId: UserId, id: AddressId): BreachEmailsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            getBreachesForProtonEmail(id.id)
        }
        .valueOrThrow

    override suspend fun getBreachesForCustomEmail(userId: UserId, id: CustomEmailId): BreachEmailsResponse =
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                getBreachesForCustomEmail(id.id)
            }
            .valueOrThrow

    override suspend fun getBreachesForAliasEmail(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): BreachEmailsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            getBreachesForAliasEmail(shareId.id, itemId.id)
        }
        .valueOrThrow

    override suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                markProtonEmailAsResolved(id.id)
            }
            .valueOrThrow
    }

    override suspend fun markAliasEmailAsResolved(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                markAliasEmailAsResolved(shareId.id, itemId.id)
            }
            .valueOrThrow
    }

    override suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId): BreachCustomEmailResponse =
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                markCustomEmailAsResolved(id.id)
            }
            .valueOrThrow

    override suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                resendVerificationCode(id.id)
            }
            .valueOrThrow
    }

    override suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                removeCustomEmail(id.id)
            }
            .valueOrThrow
    }

    override suspend fun updateGlobalProtonAddressMonitorState(
        userId: UserId,
        enabled: Boolean
    ): UpdateGlobalMonitorStateResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            updateGlobalMonitorState(
                UpdateGlobalMonitorStateRequest(
                    protonAddress = enabled,
                    aliases = null
                )
            )
        }
        .valueOrThrow

    override suspend fun updateGlobalAliasAddressMonitorState(
        userId: UserId,
        enabled: Boolean
    ): UpdateGlobalMonitorStateResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            updateGlobalMonitorState(
                UpdateGlobalMonitorStateRequest(
                    protonAddress = null,
                    aliases = enabled
                )
            )
        }
        .valueOrThrow

    override suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        id: AddressId,
        enabled: Boolean
    ) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                updateProtonAddressMonitorState(
                    id.id,
                    UpdateMonitorAddressStateRequest(
                        monitor = enabled
                    )
                )
            }
            .valueOrThrow
    }

    companion object {
        private const val INVALID_VALUE = 2001
        private const val NOT_ALLOWED = 2011
    }

}
