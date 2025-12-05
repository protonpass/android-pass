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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
import proton.android.pass.data.impl.responses.BreachProtonEmailApiModel
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachAlias
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachDomainPeek
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailReport
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

    private fun observeBreachedAliases(userId: UserId): Flow<List<BreachAlias>> = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Aliases,
        userId = userId,
        itemFlags = mapOf(ItemFlag.EmailBreached to true, ItemFlag.SkipHealthCheck to false),
        includeHidden = false
    )
        .flatMapLatest { aliases ->
            if (aliases.isEmpty()) {
                return@flatMapLatest flowOf(emptyList())
            }

            val aliasFlows = aliases.map { alias ->
                val aliasEmailId = AliasEmailId(
                    shareId = alias.shareId,
                    itemId = alias.id
                )
                localBreachDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
                    .map { breachEmails ->
                        if (breachEmails.isEmpty()) {
                            null
                        } else {
                            val aliasItem = alias.itemType as? ItemType.Alias
                                ?: return@map null

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
            }

            combine(*aliasFlows.toTypedArray()) { results ->
                results.filterNotNull()
            }
        }

    override suspend fun refreshBreaches(userId: UserId) {
        PassLogger.i(TAG, "Refreshing breaches for $userId")
        val breachesResponse: BreachesResponse = remoteBreachDataSource.getAllBreaches(userId)
        PassLogger.d(
            TAG,
            "API response domain peeks count: ${breachesResponse.breaches.domainPeeks.size}"
        )

        val breach = breachesResponse.toDomain()
        PassLogger.d(TAG, "Mapped domain peeks count: ${breach.breachedDomainPeeks.size}")

        // Store domain peeks from API response
        localBreachDataSource.upsertBreachDomainPeeks(userId, breach.breachedDomainPeeks)
        PassLogger.d(
            TAG,
            "Stored ${breach.breachedDomainPeeks.size} domain peeks from API for $userId"
        )

        // Store custom emails
        localBreachDataSource.upsertCustomEmails(userId, breach.breachedCustomEmails)

        // Store proton emails
        localBreachDataSource.upsertProtonEmails(userId, breach.breachedProtonEmails)

        // Handle dark web alias message visibility
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

    private companion object {
        private const val TAG = "BreachRepositoryImpl"
        private const val BREACH_EMAIL_RESOLVED_STATE_VALUE = 3

    }
}
