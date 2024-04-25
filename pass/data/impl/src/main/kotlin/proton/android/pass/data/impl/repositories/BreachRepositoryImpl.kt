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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.impl.local.LocalBreachesDataSource
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.responses.BreachDomainPeek
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.Breaches
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreachRepositoryImpl @Inject constructor(
    private val localUserAccessDataDataSource: LocalUserAccessDataDataSource,
    private val remote: RemoteBreachDataSource,
    private val localBreachesDataSource: LocalBreachesDataSource
) : BreachRepository {

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val shouldFetchBreachFlow: Flow<Boolean> = refreshFlow.onStart { emit(true) }

    private val aliasBreachesCache: MutableMap<Pair<UserId, Pair<ShareId, ItemId>>, List<BreachEmail>> =
        mutableMapOf()

    override fun observeAllBreaches(userId: UserId): Flow<Breach> = shouldFetchBreachFlow
        .filter { shouldFetch -> shouldFetch }
        .mapLatest { remote.getAllBreaches(userId).toDomain() }
        .distinctUntilChanged()

    override fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachCustomEmail> =
        localBreachesDataSource.observeCustomEmail(userId, customEmailId)

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> =
        localBreachesDataSource.observeCustomEmails()
            .onStart {
                remote.getCustomEmails(userId)
                    .emails
                    .customEmails
                    .map { customEmailDto -> customEmailDto.toDomain() }
                    .also { customEmails ->
                        localBreachesDataSource.upsertCustomEmails(userId, customEmails)
                    }
            }

    override suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmail =
        remote.addCustomEmail(userId, email)
            .email
            .toDomain()
            .also { customEmail -> localBreachesDataSource.upsertCustomEmail(userId, customEmail) }
            .also { refreshFlow.update { true } }

    override suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: CustomEmailId,
        code: String
    ) {
        remote.verifyCustomEmail(userId, emailId, code)

        localBreachesDataSource.getCustomEmail(userId, emailId)
            .copy(verified = true)
            .also { verifiedCustomEmail ->
                localBreachesDataSource.upsertCustomEmail(userId, verifiedCustomEmail)
            }
            .also { refreshFlow.update { true } }
    }

    override fun observeBreachesForProtonEmail(userId: UserId, id: AddressId): Flow<List<BreachEmail>> = oneShot {
        remote.getBreachesForProtonEmail(userId, id)
            .toDomain { breach -> BreachEmailId.Proton(BreachId(breach.id), id) }
    }

    override fun observeBreachesForCustomEmail(userId: UserId, id: CustomEmailId): Flow<List<BreachEmail>> =
        localBreachesDataSource.observeCustomEmailBreaches()
            .onStart {
                remote.getBreachesForCustomEmail(userId, id)
                    .toDomain { breachDto ->
                        BreachEmailId.Custom(
                            id = BreachId(breachDto.id),
                            customEmailId = id
                        )
                    }
                    .also { customEmailBreaches ->
                        localBreachesDataSource.upsertCustomEmailBreaches(userId, customEmailBreaches)
                    }
            }

    override fun observeBreachesForAliasEmail(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<BreachEmail>> = oneShot {
        aliasBreachesCache.getOrPut(userId to (shareId to itemId)) {
            remote.getBreachesForAliasEmail(userId, shareId, itemId)
                .toDomain { breach -> BreachEmailId.Alias(BreachId(breach.id), shareId, itemId) }
        }
    }

    override suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId) {
        remote.markProtonEmailAsResolved(userId, id)
    }

    override suspend fun markAliasEmailAsResolved(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ) {
        remote.markAliasEmailAsResolved(userId, shareId, itemId)
    }

    override suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId) {
        remote.markCustomEmailAsResolved(userId, id)
            .also { response ->
                localBreachesDataSource.upsertCustomEmail(userId, response.email.toDomain())
            }

        // Update locally custom email breaches state to resolved
    }

    override suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId) {
        remote.resendVerificationCode(userId, id)
            .let { localBreachesDataSource.getCustomEmail(userId, id).copy(verified = true) }
            .also { customEmail -> localBreachesDataSource.upsertCustomEmail(userId, customEmail) }
    }

    override suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId) {
        remote.removeCustomEmail(userId, id)
            .also { localBreachesDataSource.deleteCustomEmail(userId, id) }
            .also { refreshFlow.update { true } }
    }

    override suspend fun updateGlobalProtonMonitorState(userId: UserId, enabled: Boolean) {
        withContext(Dispatchers.IO) {
            remote.updateGlobalProtonAddressMonitorState(userId, enabled)
            localUserAccessDataDataSource.updateProtonMonitorState(userId, enabled)
        }
    }

    override suspend fun updateGlobalAliasMonitorState(userId: UserId, enabled: Boolean) {
        withContext(Dispatchers.IO) {
            remote.updateGlobalAliasAddressMonitorState(userId, enabled)
            localUserAccessDataDataSource.updateAliasMonitorState(userId, enabled)
        }
    }

    override suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        addressId: AddressId,
        enabled: Boolean
    ) {
        remote.updateProtonAddressMonitorState(userId, addressId, enabled)
    }


    override suspend fun updateAliasAddressMonitorState(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        enabled: Boolean
    ) {
        remote.updateAliasAddressMonitorState(userId, shareId, itemId, enabled)
    }

    fun proton.android.pass.data.impl.responses.BreachCustomEmail.toDomain() = BreachCustomEmail(
        id = CustomEmailId(customEmailId),
        email = email,
        verified = verified,
        breachCount = breachCounter,
        flags = flags,
        lastBreachTime = lastBreachTime
    )

    fun proton.android.pass.data.impl.responses.BreachProtonEmail.toDomain() = BreachProtonEmail(
        addressId = AddressId(addressId),
        email = email,
        breachCounter = breachCounter,
        flags = flags,
        lastBreachTime = lastBreachTime
    )

    private fun BreachesResponse.toDomain() = with(this.breaches) {
        Breach(
            breachesCount = emailsCount,
            breachedDomainPeeks = domainPeeks.map { domainPeek -> domainPeek.toDomain() },
            breachedCustomEmails = customEmails.map { customEmail -> customEmail.toDomain() },
            breachedProtonEmails = protonEmails.map { protonEmail -> protonEmail.toDomain() }
        )
    }

    private fun BreachDomainPeek.toDomain() = proton.android.pass.domain.breach.BreachDomainPeek(
        breachDomain = domain,
        breachTime = breachTime
    )

    private fun BreachEmailsResponse.toDomain(createId: (Breaches) -> BreachEmailId) = with(this.breachEmails) {
        breaches.map { breach ->
            BreachEmail(
                emailId = createId(breach),
                email = breach.email,
                severity = breach.severity,
                name = breach.name,
                createdAt = breach.createdAt,
                publishedAt = breach.publishedAt,
                size = breach.size,
                passwordLastChars = breach.passwordLastChars,
                exposedData = breach.exposedData.map { it.name },
                isResolved = breach.resolvedState == BREACH_EMAIL_RESOLVED_STATE_VALUE
            )
        }
    }

    private companion object {

        private const val BREACH_EMAIL_RESOLVED_STATE_VALUE = 3

    }
}
