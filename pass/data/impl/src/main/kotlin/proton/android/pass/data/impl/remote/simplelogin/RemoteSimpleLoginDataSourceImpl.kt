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

package proton.android.pass.data.impl.remote.simplelogin

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.SimpleLoginCreateAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesRequest
import proton.android.pass.data.impl.requests.SimpleLoginDeleteAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDomainRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginVerifyAliasMailboxRequest
import proton.android.pass.data.impl.responses.CodeOnlyResponse
import proton.android.pass.data.impl.responses.GetItemsResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasDomainsResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxesResponse
import proton.android.pass.data.impl.responses.SimpleLoginAliasSettingsResponse
import proton.android.pass.data.impl.responses.SimpleLoginPendingAliasesResponse
import proton.android.pass.data.impl.responses.SimpleLoginSyncStatusResponse
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class RemoteSimpleLoginDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider
) : RemoteSimpleLoginDataSource {

    override suspend fun getSimpleLoginSyncStatus(userId: UserId): SimpleLoginSyncStatusResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getSimpleLoginSyncStatus() }
        .valueOrThrow

    override suspend fun enableSimpleLoginSync(
        userId: UserId,
        request: SimpleLoginEnableSyncRequest
    ): CodeOnlyResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { enableSimpleLoginSync(request) }
        .valueOrThrow

    override suspend fun getSimpleLoginAliasDomains(userId: UserId): SimpleLoginAliasDomainsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getSimpleLoginAliasDomains() }
        .valueOrThrow

    override suspend fun updateSimpleLoginAliasDomain(
        userId: UserId,
        request: SimpleLoginUpdateAliasDomainRequest
    ): SimpleLoginAliasSettingsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { updateSimpleLoginAliasDomain(request) }
        .valueOrThrow

    override suspend fun getSimpleLoginAliasMailboxes(userId: UserId): SimpleLoginAliasMailboxesResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getSimpleLoginAliasMailboxes() }
        .valueOrThrow

    override suspend fun updateSimpleLoginAliasMailbox(
        userId: UserId,
        request: SimpleLoginUpdateAliasMailboxRequest
    ): SimpleLoginAliasSettingsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { updateSimpleLoginAliasMailbox(request) }
        .valueOrThrow

    override suspend fun getSimpleLoginAliasSettings(userId: UserId): SimpleLoginAliasSettingsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getSimpleLoginAliasSettings() }
        .valueOrThrow

    override suspend fun getSimpleLoginPendingAliases(userId: UserId): SimpleLoginPendingAliasesResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { getSimpleLoginPendingAliases() }
        .valueOrThrow

    override suspend fun createSimpleLoginPendingAliases(
        userId: UserId,
        shareId: ShareId,
        request: SimpleLoginCreatePendingAliasesRequest
    ): GetItemsResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { createSimpleLoginPendingAliases(shareId = shareId.id, request = request) }
        .valueOrThrow

    override suspend fun createSimpleLoginAliasMailbox(
        userId: UserId,
        request: SimpleLoginCreateAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { createSimpleLoginAliasMailbox(request = request) }
        .valueOrThrow

    override suspend fun verifySimpleLoginAliasMailbox(
        userId: UserId,
        mailboxId: Long,
        request: SimpleLoginVerifyAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { verifySimpleLoginAliasMailbox(mailboxId = mailboxId, request = request) }
        .valueOrThrow

    override suspend fun resendSimpleLoginAliasMailboxVerifyCode(
        userId: UserId,
        mailboxId: Long
    ): SimpleLoginAliasMailboxResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { resendSimpleLoginAliasMailboxVerifyCode(mailboxId = mailboxId) }
        .valueOrThrow

    override suspend fun deleteSimpleLoginAliasMailbox(
        userId: UserId,
        mailboxId: Long,
        request: SimpleLoginDeleteAliasMailboxRequest
    ): CodeOnlyResponse = apiProvider
        .get<PasswordManagerApi>(userId)
        .invoke { deleteSimpleLoginAliasMailbox(mailboxId = mailboxId, request = request) }
        .valueOrThrow

}
