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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.local.simplelogin.LocalSimpleLoginDataSource
import proton.android.pass.data.impl.remote.simplelogin.RemoteSimpleLoginDataSource
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesData
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesRequest
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDomainRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasMailboxRequest
import proton.android.pass.data.impl.responses.SimpleLoginAliasDomainData
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxData
import proton.android.pass.data.impl.responses.SimpleLoginAliasSettingsData
import proton.android.pass.data.impl.responses.SimpleLoginPendingAliasesData
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserAccessData
import proton.android.pass.domain.simplelogin.SimpleLoginAlias
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginPendingAliases
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import javax.inject.Inject

class SimpleLoginRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val observeVaultById: GetVaultById,
    private val localSimpleLoginDataSource: LocalSimpleLoginDataSource,
    private val remoteSimpleLoginDataSource: RemoteSimpleLoginDataSource
) : SimpleLoginRepository {

    override fun observeSyncStatus(): Flow<SimpleLoginSyncStatus> = flow {
        withUserId { userId ->
            combine(
                userAccessDataRepository.observe(userId),
                localSimpleLoginDataSource.observeSyncPreference()
            ) { nullableUserAccessData, isSimpleLoginSyncPreferenceEnabled ->
                nullableUserAccessData?.let { userAccessData ->
                    userAccessData.toSimpleLoginSyncStatus(
                        userId = userId,
                        isSimpleLoginSyncPreferenceEnabled = isSimpleLoginSyncPreferenceEnabled
                    ).let { simpleLoginSyncStatus ->
                        if (simpleLoginSyncStatus.isSyncEnabled) {
                            simpleLoginSyncStatus
                        } else {
                            // when SL sync is disabled userAccessData.pendingAliasCount always returns 0
                            // in order to get the real pendingAliasCount we need to fetch it from other endpoint
                            // https://confluence.protontech.ch/pages/viewpage.action?pageId=157843213#SLPasssync-Clientscenarios
                            simpleLoginSyncStatus.copy(
                                pendingAliasCount = remoteSimpleLoginDataSource
                                    .getSimpleLoginSyncStatus(userId)
                                    .syncStatus
                                    .pendingAliasCount
                            )
                        }
                    }
                }
            }.also { simpleLoginSyncStatus -> emitAll(simpleLoginSyncStatus.filterNotNull()) }
        }
    }

    override fun disableSyncPreference() {
        localSimpleLoginDataSource.disableSyncPreference()
    }

    override fun observeSyncPreference(): Flow<Boolean> = localSimpleLoginDataSource.observeSyncPreference()

    override suspend fun enableSync(defaultShareId: ShareId) = withUserId { userId ->
        remoteSimpleLoginDataSource.enableSimpleLoginSync(
            userId = userId,
            request = SimpleLoginEnableSyncRequest(
                defaultShareId = defaultShareId.id
            )
        )

        userAccessDataRepository.refresh(userId)
    }

    override fun observeAliasDomains(): Flow<List<SimpleLoginAliasDomain>> = flow {
        withUserId { userId ->
            remoteSimpleLoginDataSource.getSimpleLoginAliasDomains(userId).domains
                .map { simpleLoginAliasDomainData -> simpleLoginAliasDomainData.toDomain() }
                .also { simpleLoginAliasDomains -> emit(simpleLoginAliasDomains) }
        }
    }

    override suspend fun updateAliasDomain(domain: String?) {
        withUserId { userId ->
            remoteSimpleLoginDataSource.updateSimpleLoginAliasDomain(
                userId = userId,
                request = SimpleLoginUpdateAliasDomainRequest(
                    defaultAliasDomain = domain
                )
            )
                .settings
                .toDomain()
                .also(localSimpleLoginDataSource::updateAliasSettings)
        }
    }

    override fun observeAliasMailboxes(): Flow<List<SimpleLoginAliasMailbox>> = flow {
        withUserId { userId ->
            remoteSimpleLoginDataSource.getSimpleLoginAliasMailboxes(userId).mailboxes
                .map { simpleLoginAliasMailboxData -> simpleLoginAliasMailboxData.toDomain() }
                .also { simpleLoginAliasMailboxes -> emit(simpleLoginAliasMailboxes) }
        }
    }

    override suspend fun updateAliasMailbox(mailboxId: String) {
        withUserId { userId ->
            remoteSimpleLoginDataSource.updateSimpleLoginAliasMailbox(
                userId = userId,
                request = SimpleLoginUpdateAliasMailboxRequest(
                    defaultMailboxID = mailboxId
                )
            )
                .settings
                .toDomain()
                .also(localSimpleLoginDataSource::updateAliasSettings)
        }
    }

    override fun observeAliasSettings(): Flow<SimpleLoginAliasSettings> = flow {
        withUserId { userId ->
            remoteSimpleLoginDataSource.getSimpleLoginAliasSettings(userId)
                .settings
                .toDomain()
                .also(localSimpleLoginDataSource::updateAliasSettings)
        }
        emitAll(localSimpleLoginDataSource.observeAliasSettings())
    }

    override suspend fun getPendingAliases(): SimpleLoginPendingAliases = withUserId { userId ->
        remoteSimpleLoginDataSource.getSimpleLoginPendingAliases(userId)
            .pendingAliases
            .toDomain()
    }

    override suspend fun createPendingAliases(
        defaultShareId: ShareId,
        pendingAliasesItems: List<Pair<String, EncryptedCreateItem>>
    ) {
        if (pendingAliasesItems.isEmpty()) {
            return
        }

        withUserId { userId ->
            remoteSimpleLoginDataSource.createSimpleLoginPendingAliases(
                userId = userId,
                shareId = defaultShareId,
                request = SimpleLoginCreatePendingAliasesRequest(
                    items = pendingAliasesItems.map { (pendingAliasId, pendingAliasesItem) ->
                        SimpleLoginCreatePendingAliasesData(
                            pendingAliasId = pendingAliasId,
                            item = pendingAliasesItem.toRequest()
                        )
                    }
                )
            )
        }
    }

    private suspend fun <T> withUserId(block: suspend (UserId) -> T): T = accountManager
        .getPrimaryUserId()
        .firstOrNull()
        ?.let { userId -> block(userId) }
        ?: throw UserIdNotAvailableError()

    private suspend fun UserAccessData.toSimpleLoginSyncStatus(
        userId: UserId,
        isSimpleLoginSyncPreferenceEnabled: Boolean
    ) = SimpleLoginSyncStatus(
        isSyncEnabled = isSimpleLoginSyncEnabled,
        isPreferenceEnabled = isSimpleLoginSyncPreferenceEnabled,
        pendingAliasCount = simpleLoginSyncPendingAliasCount,
        defaultVault = observeVaultById(
            userId = userId,
            shareId = simpleLoginSyncDefaultShareId.let(::ShareId)
        ).first()
    )

    private fun SimpleLoginAliasDomainData.toDomain() = SimpleLoginAliasDomain(
        domain = domain,
        isDefault = isDefault
    )

    private fun SimpleLoginAliasMailboxData.toDomain() = SimpleLoginAliasMailbox(
        id = mailboxId,
        email = email,
        isDefault = isDefault,
        isVerified = verified
    )

    private fun SimpleLoginAliasSettingsData.toDomain() = SimpleLoginAliasSettings(
        defaultDomain = defaultAliasDomain,
        defaultMailboxId = defaultMailboxId
    )

    private fun SimpleLoginPendingAliasesData.toDomain() = SimpleLoginPendingAliases(
        aliases = aliases.map { alias ->
            SimpleLoginAlias(
                id = alias.pendingAliasID,
                email = alias.aliasEmail
            )
        },
        total = total,
        lastToken = lastToken
    )

}
