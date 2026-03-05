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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginPendingAliases
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import javax.inject.Inject

class FakeSimpleLoginRepository @Inject constructor() : SimpleLoginRepository {

    val observeSyncStatusResults = ArrayDeque<Result<SimpleLoginSyncStatus>>()
    var pendingAliases: SimpleLoginPendingAliases = SimpleLoginPendingAliases(
        aliases = emptyList(),
        total = 0,
        lastToken = null
    )

    val enableSyncInvocations = mutableListOf<ShareId>()
    val createPendingAliasesInvocations = mutableListOf<CreatePendingAliasesInvocation>()
    var observeSyncStatusInvocations: Int = 0

    override fun observeSyncStatus(userId: UserId, forceRefresh: Boolean): Flow<SimpleLoginSyncStatus> = flow {
        observeSyncStatusInvocations++
        val result = checkNotNull(observeSyncStatusResults.removeFirstOrNull()) {
            "No observeSyncStatus result configured"
        }
        emit(result.getOrThrow())
    }

    override fun disableSyncPreference() = Unit

    override fun observeSyncPreference(): Flow<Boolean> = emptyFlow()

    override suspend fun enableSync(defaultShareId: ShareId) {
        enableSyncInvocations.add(defaultShareId)
    }

    override fun observeAliasDomains(): Flow<List<SimpleLoginAliasDomain>> = emptyFlow()

    override suspend fun updateAliasDomain(domain: String?) = Unit

    override fun observeAliasMailboxes(): Flow<List<SimpleLoginAliasMailbox>> = emptyFlow()

    override suspend fun createAliasMailbox(email: String): SimpleLoginAliasMailbox = error("Not needed in this fake")

    override suspend fun verifyAliasMailbox(mailboxId: Long, verificationCode: String) = Unit

    override suspend fun changeAliasMailboxEmail(mailboxId: Long, email: String): SimpleLoginAliasMailbox =
        error("Not needed in this fake")

    override suspend fun cancelAliasMailboxEmailChange(mailboxId: Long) = Unit

    override suspend fun resendAliasMailboxVerificationCode(mailboxId: Long) = Unit

    override suspend fun updateAliasDefaultMailbox(mailboxId: Long) = Unit

    override fun observeAliasSettings(): Flow<SimpleLoginAliasSettings> = emptyFlow()

    override suspend fun getPendingAliases(userId: UserId): SimpleLoginPendingAliases = pendingAliases

    override suspend fun createPendingAliases(
        userId: UserId,
        defaultShareId: ShareId,
        pendingAliasesItems: List<Pair<String, EncryptedCreateItem>>
    ) {
        createPendingAliasesInvocations.add(
            CreatePendingAliasesInvocation(
                defaultShareId = defaultShareId,
                pendingAliasesItems = pendingAliasesItems
            )
        )
    }

    override fun observeAliasMailbox(mailboxId: Long): Flow<SimpleLoginAliasMailbox?> = emptyFlow()

    override suspend fun deleteAliasMailbox(mailboxId: Long, transferMailboxId: Long?) = Unit
}

data class CreatePendingAliasesInvocation(
    val defaultShareId: ShareId,
    val pendingAliasesItems: List<Pair<String, EncryptedCreateItem>>
)
