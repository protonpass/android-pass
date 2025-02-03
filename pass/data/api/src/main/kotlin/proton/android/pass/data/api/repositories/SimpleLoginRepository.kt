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

package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginPendingAliases
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus

interface SimpleLoginRepository {

    fun observeSyncStatus(userId: UserId): Flow<SimpleLoginSyncStatus>

    fun disableSyncPreference()

    fun observeSyncPreference(): Flow<Boolean>

    suspend fun enableSync(defaultShareId: ShareId)

    fun observeAliasDomains(): Flow<List<SimpleLoginAliasDomain>>

    suspend fun updateAliasDomain(domain: String?)

    fun observeAliasMailboxes(): Flow<List<SimpleLoginAliasMailbox>>

    suspend fun createAliasMailbox(email: String): SimpleLoginAliasMailbox

    suspend fun verifyAliasMailbox(mailboxId: Long, verificationCode: String)

    suspend fun changeAliasMailboxEmail(mailboxId: Long, email: String)

    suspend fun resendAliasMailboxVerificationCode(mailboxId: Long)

    suspend fun updateAliasDefaultMailbox(mailboxId: Long)

    fun observeAliasSettings(): Flow<SimpleLoginAliasSettings>

    suspend fun getPendingAliases(userId: UserId): SimpleLoginPendingAliases

    suspend fun createPendingAliases(
        userId: UserId,
        defaultShareId: ShareId,
        pendingAliasesItems: List<Pair<String, EncryptedCreateItem>>
    )

    fun observeAliasMailbox(mailboxId: Long): Flow<SimpleLoginAliasMailbox?>

    suspend fun deleteAliasMailbox(mailboxId: Long, transferMailboxId: Long?)

}
