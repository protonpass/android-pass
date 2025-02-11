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
import proton.android.pass.data.impl.requests.SimpleLoginChangeMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreateAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesRequest
import proton.android.pass.data.impl.requests.SimpleLoginDeleteAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDefaultMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDomainRequest
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

interface RemoteSimpleLoginDataSource {

    suspend fun getSimpleLoginSyncStatus(userId: UserId): SimpleLoginSyncStatusResponse

    suspend fun enableSimpleLoginSync(userId: UserId, request: SimpleLoginEnableSyncRequest): CodeOnlyResponse

    suspend fun getSimpleLoginAliasDomains(userId: UserId): SimpleLoginAliasDomainsResponse

    suspend fun updateSimpleLoginAliasDomain(
        userId: UserId,
        request: SimpleLoginUpdateAliasDomainRequest
    ): SimpleLoginAliasSettingsResponse

    suspend fun getSimpleLoginAliasMailboxes(userId: UserId): SimpleLoginAliasMailboxesResponse

    suspend fun updateSimpleLoginAliasDefaultMailbox(
        userId: UserId,
        request: SimpleLoginUpdateAliasDefaultMailboxRequest
    ): SimpleLoginAliasSettingsResponse

    suspend fun getSimpleLoginAliasSettings(userId: UserId): SimpleLoginAliasSettingsResponse

    suspend fun getSimpleLoginPendingAliases(userId: UserId): SimpleLoginPendingAliasesResponse

    suspend fun createSimpleLoginPendingAliases(
        userId: UserId,
        shareId: ShareId,
        request: SimpleLoginCreatePendingAliasesRequest
    ): GetItemsResponse

    suspend fun createSimpleLoginAliasMailbox(
        userId: UserId,
        request: SimpleLoginCreateAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    suspend fun verifySimpleLoginAliasMailbox(
        userId: UserId,
        mailboxId: Long,
        request: SimpleLoginVerifyAliasMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    suspend fun changeSimpleLoginAliasMailbox(
        userId: UserId,
        mailboxId: Long,
        request: SimpleLoginChangeMailboxRequest
    ): SimpleLoginAliasMailboxResponse

    suspend fun cancelSimpleLoginAliasMailboxEmailChange(userId: UserId, mailboxId: Long): CodeOnlyResponse

    suspend fun resendSimpleLoginAliasMailboxVerifyCode(
        userId: UserId,
        mailboxId: Long
    ): SimpleLoginAliasMailboxResponse

    suspend fun deleteSimpleLoginAliasMailbox(
        userId: UserId,
        mailboxId: Long,
        request: SimpleLoginDeleteAliasMailboxRequest
    ): CodeOnlyResponse

}
