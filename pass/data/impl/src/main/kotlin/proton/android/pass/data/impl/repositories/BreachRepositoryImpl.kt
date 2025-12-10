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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.impl.local.LocalBreachDataSource
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.responses.BreachCustomEmailApiModel
import proton.android.pass.data.impl.responses.BreachDomainPeekApiModel
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.BreachProtonEmailApiModel
import proton.android.pass.data.impl.responses.Breaches
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachAlias
import proton.android.pass.domain.breach.BreachAction
import proton.android.pass.domain.breach.BreachActionCode
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachDomainPeek
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachEmailReport
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.domain.breach.EmailFlag
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.IsDarkWebAliasMessageDismissedPreference.Dismissed
import proton.android.pass.preferences.IsDarkWebAliasMessageDismissedPreference.Show
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class BreachRepositoryImpl @Inject constructor(
    private val localUserAccessDataDataSource: LocalUserAccessDataDataSource,
    private val remoteBreachDataSource: RemoteBreachDataSource,
    private val localBreachDataSource: LocalBreachDataSource,
    private val observeItemById: ObserveItemById,
    private val observeItems: ObserveItems,
    private val internalSettings: InternalSettingsRepository
) : BreachRepository {

    override fun observeAllBreaches(userId: UserId): Flow<Breach> = combine(
        localBreachDataSource.observeBreachDomainPeeks(userId),
        localBreachDataSource.observeCustomEmails(userId),
        localBreachDataSource.observeProtonEmails(userId),
        observeBreachedAliases(userId)
    ) { domainPeeks, customEmails, protonEmails, breachedAliases ->
        val computedCount = customEmails.sumOf { it.breachCount } +
            protonEmails.sumOf { it.breachCounter } +
            breachedAliases.sumOf { it.breachCounter }

        Breach(
            breachesCount = computedCount,
            breachedDomainPeeks = domainPeeks,
            breachedCustomEmails = customEmails,
            breachedProtonEmails = protonEmails,
            breachedAliases = breachedAliases
        )
    }
        .distinctUntilChanged()

    private fun observeBreachedAliases(userId: UserId): Flow<List<BreachAlias>> =
        observeBreachedAliasItems(userId) { alias, breachEmails ->
            if (breachEmails.isEmpty()) {
                null
            } else {
                val aliasItem = alias.itemType as? ItemType.Alias
                    ?: return@observeBreachedAliasItems null

                val lastBreachTime = breachEmails.firstOrNull()?.let { breachEmail ->
                    runCatching {
                        Instant.parse(breachEmail.publishedAt)
                    }.getOrElse { Instant.DISTANT_PAST }
                        .epochSeconds
                } ?: 0L

                BreachAlias(
                    shareId = alias.shareId,
                    itemId = alias.id,
                    email = aliasItem.aliasEmail,
                    breachCounter = breachEmails.filter { !it.isResolved }.size,
                    flags = alias.itemFlags.value,
                    lastBreachTime = lastBreachTime
                )
            }
        }
            .map { results -> results.filterNotNull() }

    override suspend fun refreshBreaches(userId: UserId) {
        PassLogger.i(TAG, "Refreshing breaches for $userId")
        val breachesResponse: BreachesResponse = remoteBreachDataSource.getAllBreaches(userId)
        PassLogger.d(
            TAG,
            "API response domain peeks count: ${breachesResponse.breaches.domainPeeks.size}"
        )

        val breach = breachesResponse.toDomain()
        PassLogger.d(TAG, "Mapped domain peeks count: ${breach.breachedDomainPeeks.size}")

        localBreachDataSource.upsertBreachDomainPeeks(userId, breach.breachedDomainPeeks)
        PassLogger.d(
            TAG,
            "Stored ${breach.breachedDomainPeeks.size} domain peeks from API for $userId"
        )

        localBreachDataSource.upsertCustomEmails(userId, breach.breachedCustomEmails)

        localBreachDataSource.upsertProtonEmails(userId, breach.breachedProtonEmails)

        refreshBreachEmails(userId, breach)

        safeRunCatching {
            val showMessage = internalSettings.getDarkWebAliasMessageVisibility().first()
            if (showMessage == Show && !breachesResponse.breaches.hasCustomDomains) {
                internalSettings.setDarkWebAliasMessageVisibility(Dismissed)
            }
        }

        PassLogger.i(TAG, "Finished refreshing breaches for $userId")
    }

    override fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachEmailReport.Custom> =
        localBreachDataSource
            .observeCustomEmail(userId, customEmailId)
            .map { it.toCustomReport() }

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> =
        localBreachDataSource.observeCustomEmails(userId)

    override suspend fun addCustomEmail(userId: UserId, email: String): BreachCustomEmail =
        remoteBreachDataSource.addCustomEmail(userId, email)
            .email
            .toDomain()
            .also { customEmail -> localBreachDataSource.upsertCustomEmail(userId, customEmail) }

    override suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: CustomEmailId,
        code: String
    ) {
        safeRunCatching {
            remoteBreachDataSource.verifyCustomEmail(userId, emailId, code)
        }.onSuccess {
            PassLogger.i(TAG, "Custom email verified successfully")
            localBreachDataSource.getCustomEmail(userId, emailId)
                .copy(verified = true)
                .also { verifiedCustomEmail ->
                    localBreachDataSource.upsertCustomEmail(userId, verifiedCustomEmail)
                }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error verifying custom email")
            PassLogger.w(TAG, error)
            when (error) {
                is CustomEmailDoesNotExistException ->
                    localBreachDataSource.deleteCustomEmail(userId, emailId)

                else -> throw error
            }
        }
    }

    override fun observeAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<BreachEmailReport.Alias> = combine(
        observeItemById(aliasEmailId.shareId, aliasEmailId.itemId)
            .map { item ->
                item ?: throw ItemNotFoundError(aliasEmailId.itemId, aliasEmailId.shareId)
            },
        localBreachDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
    ) { aliasItem, aliasEmailBreaches ->
        BreachEmailReport.Alias(
            id = aliasEmailId,
            email = (aliasItem.itemType as ItemType.Alias).aliasEmail,
            breachCount = aliasEmailBreaches.filter { !it.isResolved }.size,
            isMonitoringDisabled = aliasItem.hasSkippedHealthCheck,
            lastBreachTime = aliasEmailBreaches.firstOrNull()?.let { aliasEmailBreach ->
                runCatching { Instant.parse(aliasEmailBreach.publishedAt) }
                    .getOrElse { Instant.DISTANT_PAST }
                    .epochSeconds
                    .toInt()
            }
        )
    }

    override fun observeProtonEmail(userId: UserId, addressId: AddressId): Flow<BreachEmailReport.Proton> =
        localBreachDataSource.observeProtonEmail(userId, addressId)
            .map { it.toProtonReport() }

    override fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>> =
        localBreachDataSource.observeProtonEmails(userId)

    override fun observeBreachesForCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>> =
        localBreachDataSource.observeCustomEmailBreaches(userId, customEmailId)

    override fun observeBreachesForProtonEmail(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>> =
        localBreachDataSource.observeProtonEmailBreaches(userId, addressId)

    override fun observeBreachesForAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>> =
        localBreachDataSource.observeAliasEmailBreaches(userId, aliasEmailId)

    override suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId) {
        remoteBreachDataSource.markProtonEmailAsResolved(userId, id)

        localBreachDataSource.getProtonEmail(userId, id)
            .copy(flags = BREACH_EMAIL_RESOLVED_STATE_VALUE)
            .also { verifiedProtonEmail ->
                localBreachDataSource.upsertProtonEmail(userId, verifiedProtonEmail)
            }

        localBreachDataSource.observeProtonEmailBreaches(userId, id)
            .first()
            .map { protonEmailBreach -> protonEmailBreach.copy(isResolved = true) }
            .also { resolvedProtonEmailBreaches ->
                localBreachDataSource.upsertProtonEmailBreaches(
                    userId = userId,
                    addressId = id,
                    protonEmailBreaches = resolvedProtonEmailBreaches
                )
            }
    }

    override suspend fun markAliasEmailAsResolved(userId: UserId, aliasEmailId: AliasEmailId) {
        remoteBreachDataSource.markAliasEmailAsResolved(userId, aliasEmailId.shareId, aliasEmailId.itemId)

        localBreachDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
            .first()
            .map { aliasEmailBreach -> aliasEmailBreach.copy(isResolved = true) }
            .also { resolvedAliasEmailBreach ->
                localBreachDataSource.upsertAliasEmailBreaches(
                    userId = userId,
                    aliasEmailId = aliasEmailId,
                    aliasEmailBreaches = resolvedAliasEmailBreach
                )
            }
    }

    override suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId) {
        remoteBreachDataSource.markCustomEmailAsResolved(userId, id)
            .also { response ->
                localBreachDataSource.upsertCustomEmail(userId, response.email.toDomain())
            }

        localBreachDataSource.observeCustomEmailBreaches(userId, id)
            .first()
            .map { customEmailBreach -> customEmailBreach.copy(isResolved = true) }
            .also { resolvedCustomEmailBreaches ->
                localBreachDataSource.upsertCustomEmailBreaches(
                    userId = userId,
                    customEmailId = id,
                    customEmailBreaches = resolvedCustomEmailBreaches
                )
            }
    }

    override suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId) {
        remoteBreachDataSource.resendVerificationCode(userId, id)
    }

    override suspend fun removeCustomEmail(userId: UserId, id: CustomEmailId) {
        remoteBreachDataSource.removeCustomEmail(userId, id)
            .also { localBreachDataSource.deleteCustomEmail(userId, id) }
    }

    override suspend fun updateGlobalProtonMonitorState(userId: UserId, enabled: Boolean) {
        remoteBreachDataSource.updateGlobalProtonAddressMonitorState(userId, enabled)
        localUserAccessDataDataSource.updateProtonMonitorState(userId, enabled)
    }

    override suspend fun updateGlobalAliasMonitorState(userId: UserId, enabled: Boolean) {
        remoteBreachDataSource.updateGlobalAliasAddressMonitorState(userId, enabled)
        localUserAccessDataDataSource.updateAliasMonitorState(userId, enabled)
    }

    override suspend fun updateProtonAddressMonitorState(
        userId: UserId,
        addressId: AddressId,
        enabled: Boolean
    ) {
        remoteBreachDataSource.updateProtonAddressMonitorState(userId, addressId, enabled)

        localBreachDataSource.getProtonEmail(userId, addressId)
            .let { breachProtonEmail ->
                breachProtonEmail.copy(
                    flags = breachProtonEmail.flags xor EmailFlag.MonitoringDisabled.value
                )
            }
            .also { flaggedBreachProtonEmail ->
                localBreachDataSource.upsertProtonEmail(userId, flaggedBreachProtonEmail)
            }
    }

    fun BreachCustomEmailApiModel.toDomain() = BreachCustomEmail(
        id = CustomEmailId(customEmailId),
        email = email,
        verified = verified,
        breachCount = breachCounter,
        flags = flags,
        lastBreachTime = lastBreachTime
    )

    fun BreachProtonEmailApiModel.toDomain() = BreachProtonEmail(
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
            breachedProtonEmails = protonEmails.map { protonEmail -> protonEmail.toDomain() },
            breachedAliases = emptyList()
        )
    }

    private fun BreachDomainPeekApiModel.toDomain() = BreachDomainPeek(
        breachDomain = domain,
        breachTime = breachTime
    )

    private fun BreachCustomEmail.toCustomReport(): BreachEmailReport.Custom = BreachEmailReport.Custom(
        id = this.id,
        isVerified = this.verified,
        email = this.email,
        breachCount = this.breachCount,
        flags = this.flags,
        lastBreachTime = this.lastBreachTime
    )

    private fun BreachProtonEmail.toProtonReport(): BreachEmailReport.Proton = BreachEmailReport.Proton(
        addressId = this.addressId,
        email = this.email,
        breachCount = this.breachCounter,
        flags = this.flags,
        lastBreachTime = this.lastBreachTime
    )

    override fun observeHasBreaches(userId: UserId): Flow<Boolean> = combine(
        localBreachDataSource.observeTotalBreachCount(userId),
        observeBreachedAliasesCount(userId)
    ) { customAndProtonCount, aliasCount ->
        customAndProtonCount + aliasCount > 0
    }

    private fun observeBreachedAliasesCount(userId: UserId): Flow<Int> =
        observeBreachedAliasItems(userId) { _, breachEmails ->
            breachEmails.filter { !it.isResolved }.size
        }
            .map { results -> results.sum() }

    private suspend fun refreshBreachEmails(userId: UserId, breach: Breach) {
        refreshCustomEmailBreaches(userId, breach.breachedCustomEmails)
        refreshProtonEmailBreaches(userId, breach.breachedProtonEmails)
        refreshAliasEmailBreaches(userId)
    }

    private suspend fun refreshCustomEmailBreaches(userId: UserId, breachedCustomEmails: List<BreachCustomEmail>) {
        breachedCustomEmails.forEach { customEmail ->
            remoteBreachDataSource.getBreachesForCustomEmail(userId, customEmail.id)
                .toDomain { breachDto ->
                    BreachEmailId.Custom(
                        id = BreachId(breachDto.id),
                        customEmailId = customEmail.id
                    )
                }
                .also { customEmailBreaches ->
                    localBreachDataSource.upsertCustomEmailBreaches(
                        userId = userId,
                        customEmailId = customEmail.id,
                        customEmailBreaches = customEmailBreaches
                    )
                }
        }
    }

    private suspend fun refreshProtonEmailBreaches(userId: UserId, breachedProtonEmails: List<BreachProtonEmail>) {
        breachedProtonEmails.forEach { protonEmail ->
            remoteBreachDataSource.getBreachesForProtonEmail(userId, protonEmail.addressId)
                .toDomain { breachDto ->
                    BreachEmailId.Proton(
                        id = BreachId(breachDto.id),
                        addressId = protonEmail.addressId
                    )
                }
                .also { protonEmailBreaches ->
                    localBreachDataSource.upsertProtonEmailBreaches(
                        userId = userId,
                        addressId = protonEmail.addressId,
                        protonEmailBreaches = protonEmailBreaches
                    )
                }
        }
    }

    private suspend fun refreshAliasEmailBreaches(userId: UserId) {
        val breachedAliases = observeItems(
            selection = ShareSelection.AllShares,
            itemState = ItemState.Active,
            filter = ItemTypeFilter.Aliases,
            userId = userId,
            itemFlags = mapOf(ItemFlag.EmailBreached to true, ItemFlag.SkipHealthCheck to false),
            includeHidden = false
        ).first()

        breachedAliases.forEach { alias ->
            val aliasEmailId = AliasEmailId(alias.shareId, alias.id)
            remoteBreachDataSource.getBreachesForAliasEmail(userId, alias.shareId, alias.id)
                .toDomain { breachDto ->
                    BreachEmailId.Alias(
                        id = BreachId(breachDto.id),
                        shareId = alias.shareId,
                        itemId = alias.id
                    )
                }
                .also { aliasEmailBreaches ->
                    localBreachDataSource.upsertAliasEmailBreaches(
                        userId = userId,
                        aliasEmailId = aliasEmailId,
                        aliasEmailBreaches = aliasEmailBreaches
                    )
                }
        }
    }

    private inline fun <T> observeBreachedAliasItems(
        userId: UserId,
        crossinline transform: (Item, List<BreachEmail>) -> T
    ): Flow<List<T>> = combine(
        observeItems(
            selection = ShareSelection.AllShares,
            itemState = ItemState.Active,
            filter = ItemTypeFilter.Aliases,
            userId = userId,
            itemFlags = mapOf(ItemFlag.EmailBreached to true, ItemFlag.SkipHealthCheck to false),
            includeHidden = false
        ),
        localBreachDataSource.observeAllAliasEmailBreaches(userId)
    ) { aliases, allBreaches ->
        if (aliases.isEmpty()) return@combine emptyList()

        val breachesByAlias = allBreaches.groupBy { breach ->
            val emailId = breach.emailId as BreachEmailId.Alias
            emailId.shareId to emailId.itemId
        }

        aliases.map { alias ->
            val aliasKey = alias.shareId to alias.id
            val breachEmails = breachesByAlias[aliasKey] ?: emptyList()
            transform(alias, breachEmails)
        }
    }

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
                isResolved = breach.resolvedState == BREACH_EMAIL_RESOLVED_STATE_VALUE,
                actions = breach.actions.map {
                    BreachAction(
                        name = it.name,
                        code = BreachActionCode.from(it.code),
                        url = it.urls?.firstOrNull()
                    )
                }
            )
        }
    }

    private companion object {
        private const val TAG = "BreachRepositoryImpl"
        private const val BREACH_EMAIL_RESOLVED_STATE_VALUE = 3

    }
}
