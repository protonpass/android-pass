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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.data.impl.local.simplelogin.LocalSimpleLoginDataSource
import proton.android.pass.data.impl.remote.simplelogin.RemoteSimpleLoginDataSource
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.responses.SimpleLoginAliasDomainData
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxData
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserAccessData
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import javax.inject.Inject

class SimpleLoginRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val observeVaultById: GetVaultById,
    private val localSimpleLoginDataSource: LocalSimpleLoginDataSource,
    private val remoteSimpleLoginDataSource: RemoteSimpleLoginDataSource
) : SimpleLoginRepository {

    private val simpleLoginSyncStatus = accountManager.getPrimaryUserId()
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(None)
            } else {
                combine(
                    userAccessDataRepository.observe(userId),
                    localSimpleLoginDataSource.observeSyncPreference()
                ) { userAccessData, isSimpleLoginSyncPreferenceEnabled ->
                    userAccessData?.toSimpleLoginSyncStatus(
                        userId = userId,
                        isSimpleLoginSyncPreferenceEnabled = isSimpleLoginSyncPreferenceEnabled
                    ).toOption()
                }
            }
        }

    override fun observeSyncStatus(): Flow<Option<SimpleLoginSyncStatus>> = flow {
        emit(simpleLoginSyncStatus.first())

        val userId = accountManager.getPrimaryUserId()
            .firstOrNull()
            ?: throw UserIdNotAvailableError()

        userAccessDataRepository.refresh(userId)

        emitAll(simpleLoginSyncStatus)
    }

    override fun disableSyncPreference() {
        localSimpleLoginDataSource.disableSyncPreference()
    }

    override fun observeSyncPreference(): Flow<Boolean> = localSimpleLoginDataSource.observeSyncPreference()

    override suspend fun enableSync(defaultShareId: ShareId) {
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull()
            ?: throw UserIdNotAvailableError()

        SimpleLoginEnableSyncRequest(
            defaultShareId = defaultShareId.id
        ).also { request ->
            remoteSimpleLoginDataSource.enableSimpleLoginSync(userId, request)
        }

        userAccessDataRepository.refresh(userId)
    }

    override fun observeAliasDomains(): Flow<List<SimpleLoginAliasDomain>> = accountManager.getPrimaryUserId()
        .flatMapLatest { userId ->
            if (userId == null) {
                emptyList()
            } else {
                remoteSimpleLoginDataSource.getSimpleLoginAliasSettings(userId)
                    .also {
                        println("JIBIRI: settings: $it")
                    }

                remoteSimpleLoginDataSource.getSimpleLoginAliasDomains(userId)
                    .domains
                    .map { simpleLoginAliasDomainData -> simpleLoginAliasDomainData.toDomain() }
            }.let(::flowOf)
        }

    override fun observeAliasMailboxes(): Flow<List<SimpleLoginAliasMailbox>> = accountManager.getPrimaryUserId()
        .flatMapLatest { userId ->
            if (userId == null) {
                emptyList()
            } else {
                remoteSimpleLoginDataSource.getSimpleLoginAliasMailboxes(userId)
                    .mailboxes
                    .map { simpleLoginAliasMailboxData -> simpleLoginAliasMailboxData.toDomain() }
            }.let(::flowOf)
        }

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
        isDefault = isDefault
    )

}
