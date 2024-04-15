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
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.BreachAddEmailRequest
import proton.android.pass.data.impl.requests.BreachVerifyEmailRequest
import proton.android.pass.data.impl.responses.BreachCustomEmailResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailsResponse
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmailId
import javax.inject.Inject

interface RemoteBreachDataSource {
    suspend fun getCustomEmails(userId: UserId): BreachCustomEmailsResponse
    suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmailResponse
    suspend fun verifyCustomEmail(
        userId: UserId,
        id: BreachCustomEmailId,
        code: String
    )

    suspend fun getBreachesForCustomEmail(userId: UserId, id: BreachCustomEmailId): BreachEmailsResponse

    suspend fun getBreachesForAlias(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): BreachEmailsResponse
}

class RemoteBreachDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteBreachDataSource {
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
        id: BreachCustomEmailId,
        code: String
    ) {
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                verifyBreachEmail(emailId = id.id, request = BreachVerifyEmailRequest(code))
            }
            .valueOrThrow
    }

    override suspend fun getBreachesForCustomEmail(userId: UserId, id: BreachCustomEmailId): BreachEmailsResponse =
        apiProvider
            .get<PasswordManagerApi>(userId)
            .invoke {
                getBreachesForCustomEmail(id.id)
            }
            .valueOrThrow

    override suspend fun getBreachesForAlias(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): BreachEmailsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke {
            getBreachesForAlias(shareId.id, itemId.id)
        }
        .valueOrThrow
}
