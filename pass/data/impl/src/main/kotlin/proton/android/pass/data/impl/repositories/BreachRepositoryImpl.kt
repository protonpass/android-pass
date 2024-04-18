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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.repositories.BreachRepository
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BreachRepositoryImpl @Inject constructor(
    private val remote: RemoteBreachDataSource
) : BreachRepository {

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val customEmailBreachesCache: MutableMap<Pair<UserId, BreachId>, List<BreachEmail>> =
        mutableMapOf()
    private val aliasBreachesCache: MutableMap<Pair<UserId, Pair<ShareId, ItemId>>, List<BreachEmail>> =
        mutableMapOf()

    override fun observeBreach(userId: UserId): Flow<Breach> = refreshFlow
        .filter { shouldRefresh -> shouldRefresh }
        .mapLatest { remote.getAllBreaches(userId).toDomain() }
        .distinctUntilChanged()

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> = refreshFlow
        .filter { it }
        .mapLatest { refreshEmails(userId) }
        .onEach {
            refreshFlow.update { false }
        }
        .distinctUntilChanged()
        .onStart {
            emit(refreshEmails(userId))
        }

    override suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmail =
        remote.addCustomEmail(userId, email).email.toDomain().also {
            refreshFlow.update { true }
        }

    override suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: BreachEmailId.Custom,
        code: String
    ) {
        remote.verifyCustomEmail(userId, emailId, code)
        refreshFlow.update { true }
    }

    override fun observeBreachesForProtonEmail(userId: UserId, id: AddressId): Flow<List<BreachEmail>> = oneShot {
        remote.getBreachesForProtonEmail(userId, id)
            .toDomain { breach -> BreachEmailId.Proton(BreachId(breach.id), id) }
    }

    override fun observeBreachesForCustomEmail(userId: UserId, id: BreachEmailId.Custom): Flow<List<BreachEmail>> =
        oneShot {
            customEmailBreachesCache.getOrPut(userId to id.id) {
                remote.getBreachesForCustomEmail(userId, id)
                    .toDomain { breach -> BreachEmailId.Custom(BreachId(breach.id)) }
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

    private suspend fun refreshEmails(userId: UserId): List<BreachCustomEmail> {
        val response = remote.getCustomEmails(userId)
        return response.emails.customEmails.map { it.toDomain() }
    }

    fun proton.android.pass.data.impl.responses.BreachCustomEmail.toDomain() = BreachCustomEmail(
        id = BreachEmailId.Custom(BreachId(customEmailId)),
        email = email,
        verified = verified,
        breachCount = breachCounter
    )

    private fun BreachesResponse.toDomain() = with(this.breaches) {
        Breach(
            breachesCount = emailsCount,
            breachedDomainPeeks = domainPeeks.map { domainPeek -> domainPeek.toDomain() },
            breachedCustomEmails = customEmails.map { customEmail -> customEmail.toDomain() }
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
                exposedData = breach.exposedData.map { it.name }
            )
        }
    }
}
