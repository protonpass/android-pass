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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.data.api.errors.EmailAlreadyInUseError
import proton.android.pass.data.api.errors.ErrorCodes
import proton.android.pass.data.api.errors.InvalidVerificationCodeError
import proton.android.pass.data.api.errors.InvalidVerificationCodeLimitError
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.errors.getProtonErrorCode
import proton.android.pass.data.api.repositories.SimpleLoginRepository
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.impl.extensions.toRequest
import proton.android.pass.data.impl.local.simplelogin.LocalSimpleLoginDataSource
import proton.android.pass.data.impl.remote.simplelogin.RemoteSimpleLoginDataSource
import proton.android.pass.data.impl.requests.SimpleLoginChangeMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreateAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesData
import proton.android.pass.data.impl.requests.SimpleLoginCreatePendingAliasesRequest
import proton.android.pass.data.impl.requests.SimpleLoginDeleteAliasMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginEnableSyncRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDefaultMailboxRequest
import proton.android.pass.data.impl.requests.SimpleLoginUpdateAliasDomainRequest
import proton.android.pass.data.impl.requests.SimpleLoginVerifyAliasMailboxRequest
import proton.android.pass.data.impl.responses.SimpleLoginAliasDomainData
import proton.android.pass.data.impl.responses.SimpleLoginAliasMailboxData
import proton.android.pass.data.impl.responses.SimpleLoginAliasSettingsData
import proton.android.pass.data.impl.responses.SimpleLoginPendingAliasesData
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserAccessData
import proton.android.pass.domain.Vault
import proton.android.pass.domain.simplelogin.SimpleLoginAlias
import proton.android.pass.domain.simplelogin.SimpleLoginAliasDomain
import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.domain.simplelogin.SimpleLoginAliasSettings
import proton.android.pass.domain.simplelogin.SimpleLoginPendingAliases
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@Suppress("TooManyFunctions")
class SimpleLoginRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val userAccessDataRepository: UserAccessDataRepository,
    private val observeVaultById: GetVaultByShareId,
    private val localSimpleLoginDataSource: LocalSimpleLoginDataSource,
    private val remoteSimpleLoginDataSource: RemoteSimpleLoginDataSource
) : SimpleLoginRepository {

    private val userIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .distinctUntilChanged()

    override fun observeSyncStatus(userId: UserId): Flow<SimpleLoginSyncStatus> = syncStatusForUser(userId)
        .filterNotNull()
        .onStart { refreshSyncStatus(userId) }

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

        refreshSyncStatus(userId)
    }

    override fun observeAliasDomains(): Flow<List<SimpleLoginAliasDomain>> = userIdFlow
        .flatMapLatest { userId ->
            flow {
                val localAliasDomains =
                    localSimpleLoginDataSource.observeAliasDomains(userId).first()

                if (localAliasDomains.isNotEmpty()) {
                    emit(localAliasDomains)
                }

                runCatching { remoteSimpleLoginDataSource.getSimpleLoginAliasDomains(userId) }
                    .onFailure { error ->
                        PassLogger.w(TAG, "There was an error fetching alias domains")
                        PassLogger.w(TAG, error)
                        if (localAliasDomains.isEmpty()) throw error
                    }
                    .onSuccess { response ->
                        response.domains
                            .map { simpleLoginAliasDomainData ->
                                simpleLoginAliasDomainData.toDomain()
                            }
                            .also { remoteAliasDomains ->
                                localSimpleLoginDataSource.refreshAliasDomains(
                                    userId = userId,
                                    aliasDomains = remoteAliasDomains
                                )
                            }
                    }

                emitAll(localSimpleLoginDataSource.observeAliasDomains(userId))
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
                .also { newAliasSettings ->
                    localSimpleLoginDataSource.updateAliasSettings(newAliasSettings)
                    localSimpleLoginDataSource.updateDefaultAliasDomain(
                        userId = userId,
                        newDefaultAliasDomain = newAliasSettings.defaultDomain
                    )
                }
        }
    }

    override fun observeAliasMailbox(mailboxId: Long): Flow<SimpleLoginAliasMailbox?> = userIdFlow
        .flatMapLatest { userId ->
            localSimpleLoginDataSource.observeAliasMailbox(userId, mailboxId)
        }

    override fun observeAliasMailboxes(): Flow<List<SimpleLoginAliasMailbox>> = userIdFlow
        .flatMapLatest { userId ->
            flow {
                val localAliasMailboxes =
                    localSimpleLoginDataSource.observeAliasMailboxes(userId).first()

                if (localAliasMailboxes.isNotEmpty()) {
                    emit(localAliasMailboxes)
                }

                runCatching { remoteSimpleLoginDataSource.getSimpleLoginAliasMailboxes(userId) }
                    .onFailure { error ->
                        PassLogger.w(TAG, "There was an error fetching alias mailboxes")
                        PassLogger.w(TAG, error)
                        if (localAliasMailboxes.isEmpty()) throw error
                    }
                    .onSuccess { response ->
                        response.mailboxes
                            .map { simpleLoginAliasMailboxData ->
                                simpleLoginAliasMailboxData.toDomain()
                            }
                            .also { remoteAliasMailboxes ->
                                localSimpleLoginDataSource.refreshAliasMailboxes(
                                    userId = userId,
                                    aliasMailboxes = remoteAliasMailboxes
                                )
                            }
                    }

                emitAll(localSimpleLoginDataSource.observeAliasMailboxes(userId))
            }
        }

    override suspend fun createAliasMailbox(email: String): SimpleLoginAliasMailbox = withUserId { userId ->
        remoteSimpleLoginDataSource.createSimpleLoginAliasMailbox(
            userId = userId,
            request = SimpleLoginCreateAliasMailboxRequest(email = email)
        )
            .mailbox
            .toDomain()
    }

    override suspend fun verifyAliasMailbox(mailboxId: Long, verificationCode: String) {
        withUserId { userId ->
            runCatching {
                remoteSimpleLoginDataSource.verifySimpleLoginAliasMailbox(
                    userId = userId,
                    mailboxId = mailboxId,
                    request = SimpleLoginVerifyAliasMailboxRequest(code = verificationCode)
                )
            }.onFailure { error ->
                when (error.getProtonErrorCode()) {
                    ErrorCodes.INVALID_VERIFICATION_CODE -> throw InvalidVerificationCodeError
                    ErrorCodes.INVALID_VERIFICATION_CODE_LIMIT -> {
                        localSimpleLoginDataSource.deleteAliasMailbox(userId, mailboxId)
                        throw InvalidVerificationCodeLimitError
                    }

                    else -> throw error
                }
            }
        }
    }

    override suspend fun changeAliasMailboxEmail(mailboxId: Long, email: String): SimpleLoginAliasMailbox =
        withUserId { userId ->
            runCatching {
                val mailbox = remoteSimpleLoginDataSource.changeSimpleLoginAliasMailbox(
                    userId = userId,
                    mailboxId = mailboxId,
                    request = SimpleLoginChangeMailboxRequest(email = email)
                ).mailbox.toDomain()
                localSimpleLoginDataSource.updateAliasMailbox(userId, mailbox)
                mailbox
            }.onFailure { error ->
                when (error.getProtonErrorCode()) {
                    ErrorCodes.EMAIL_ALREADY_IN_USE -> throw EmailAlreadyInUseError()
                    else -> throw error
                }
            }.getOrThrow()
        }

    override suspend fun cancelAliasMailboxEmailChange(mailboxId: Long) {
        withUserId { userId ->
            remoteSimpleLoginDataSource.cancelSimpleLoginAliasMailboxEmailChange(
                userId = userId,
                mailboxId = mailboxId
            )
            val mailbox = localSimpleLoginDataSource.observeAliasMailbox(userId, mailboxId).first()
            val updatedMailbox = mailbox?.copy(pendingEmail = null) ?: return@withUserId
            localSimpleLoginDataSource.updateAliasMailbox(userId, updatedMailbox)
        }
    }

    override suspend fun resendAliasMailboxVerificationCode(mailboxId: Long) {
        withUserId { userId ->
            remoteSimpleLoginDataSource.resendSimpleLoginAliasMailboxVerifyCode(
                userId = userId,
                mailboxId = mailboxId
            )
        }
    }

    override suspend fun updateAliasDefaultMailbox(mailboxId: Long) {
        withUserId { userId ->
            remoteSimpleLoginDataSource.updateSimpleLoginAliasDefaultMailbox(
                userId = userId,
                request = SimpleLoginUpdateAliasDefaultMailboxRequest(
                    defaultMailboxID = mailboxId
                )
            )
                .settings
                .toDomain()
                .also { aliasSettings ->
                    localSimpleLoginDataSource.updateAliasSettings(aliasSettings)
                    localSimpleLoginDataSource.updateDefaultAliasMailbox(
                        userId = userId,
                        mailboxId = aliasSettings.defaultMailboxId
                    )
                }
        }
    }

    override suspend fun deleteAliasMailbox(mailboxId: Long, transferMailboxId: Long?) {
        withUserId { userId: UserId ->
            remoteSimpleLoginDataSource.deleteSimpleLoginAliasMailbox(
                userId = userId,
                mailboxId = mailboxId,
                request = SimpleLoginDeleteAliasMailboxRequest(
                    transferMailboxId = transferMailboxId
                )
            ).also {
                localSimpleLoginDataSource.deleteAliasMailbox(userId, mailboxId)
            }
        }
    }

    override fun observeAliasSettings(): Flow<SimpleLoginAliasSettings> = userIdFlow
        .onEach { userId ->
            remoteSimpleLoginDataSource.getSimpleLoginAliasSettings(userId)
                .settings
                .toDomain()
                .also(localSimpleLoginDataSource::updateAliasSettings)
        }
        .flatMapLatest {
            localSimpleLoginDataSource.observeAliasSettings()
        }

    override suspend fun getPendingAliases(userId: UserId): SimpleLoginPendingAliases =
        remoteSimpleLoginDataSource.getSimpleLoginPendingAliases(userId)
            .pendingAliases
            .toDomain()

    override suspend fun createPendingAliases(
        userId: UserId,
        defaultShareId: ShareId,
        pendingAliasesItems: List<Pair<String, EncryptedCreateItem>>
    ) {
        if (pendingAliasesItems.isEmpty()) {
            return
        }

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

    private suspend fun <T> withUserId(block: suspend (UserId) -> T): T = accountManager
        .getPrimaryUserId()
        .firstOrNull()
        ?.let { userId -> block(userId) }
        ?: throw UserIdNotAvailableError()

    private suspend fun refreshSyncStatus(userId: UserId) {
        userAccessDataRepository.refresh(userId)
        val userAccessData = userAccessDataRepository.observe(userId)
            .filterNotNull()
            .first()

        // when SL sync is disabled userAccessData.pendingAliasCount always returns 0
        // in order to get the real pendingAliasCount we need to fetch it from other endpoint
        // https://confluence.protontech.ch/pages/viewpage.action?pageId=157843213#SLPasssync-Clientscenarios
        if (!userAccessData.isSimpleLoginSyncEnabled) {
            userAccessDataRepository.update(
                userId = userId,
                userAccessData = userAccessData.copy(
                    simpleLoginSyncPendingAliasCount = remoteSimpleLoginDataSource
                        .getSimpleLoginSyncStatus(userId)
                        .syncStatus
                        .pendingAliasCount
                )
            )
        }
    }

    private fun userAccessDataWithVaultFlow(userId: UserId): Flow<Option<Pair<UserAccessData, Vault>>> =
        userAccessDataRepository.observe(userId)
            .mapLatest { userAccessData: UserAccessData? ->
                if (userAccessData == null) return@mapLatest None
                val shareId = ShareId(userAccessData.simpleLoginSyncDefaultShareId)
                val defaultVault = observeVaultById(
                    userId = userId,
                    shareId = shareId
                ).firstOrNull()

                if (defaultVault == null) {
                    userAccessDataRepository.refresh(userId)
                    None
                } else {
                    (userAccessData to defaultVault).some()
                }
            }

    private fun syncStatusForUser(userId: UserId): Flow<SimpleLoginSyncStatus?> = combine(
        userAccessDataWithVaultFlow(userId),
        localSimpleLoginDataSource.observeSyncPreference()
    ) { optionalUserAccessAndVault, isSimpleLoginSyncPreferenceEnabled ->
        val (userAccess, vault) = optionalUserAccessAndVault.value() ?: return@combine null
        userAccess.toSimpleLoginSyncStatus(isSimpleLoginSyncPreferenceEnabled, vault)
    }

    private fun UserAccessData.toSimpleLoginSyncStatus(
        isSimpleLoginSyncPreferenceEnabled: Boolean,
        defaultVault: Vault
    ) = SimpleLoginSyncStatus(
        isSyncEnabled = isSimpleLoginSyncEnabled,
        isPreferenceEnabled = isSimpleLoginSyncPreferenceEnabled,
        pendingAliasCount = simpleLoginSyncPendingAliasCount,
        canManageAliases = canManageSimpleLoginAliases,
        defaultVault = defaultVault
    )

    private fun SimpleLoginAliasDomainData.toDomain() = SimpleLoginAliasDomain(
        domain = domain,
        isDefault = isDefault,
        isCustom = isCustom,
        isPremium = isPremium,
        isVerified = isVerified
    )

    private fun SimpleLoginAliasMailboxData.toDomain() = SimpleLoginAliasMailbox(
        id = mailboxId,
        email = email,
        pendingEmail = pendingEmail,
        isDefault = isDefault,
        isVerified = verified,
        aliasCount = aliasCount
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

    private companion object {

        private const val TAG = "SimpleLoginRepositoryImpl"

    }

}

