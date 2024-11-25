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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.impl.local.LocalBreachesDataSource
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.data.impl.responses.BreachDomainPeek
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.Breaches
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.Breach
import proton.android.pass.domain.breach.BreachAction
import proton.android.pass.domain.breach.BreachActionCode
import proton.android.pass.domain.breach.BreachCustomEmail
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
    private val remote: RemoteBreachDataSource,
    private val localBreachesDataSource: LocalBreachesDataSource,
    private val observeItemById: ObserveItemById,
    private val internalSettings: InternalSettingsRepository
) : BreachRepository {

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val shouldFetchBreachFlow: Flow<Boolean> = refreshFlow.onStart { emit(true) }

    override fun observeAllBreaches(userId: UserId): Flow<Breach> = shouldFetchBreachFlow
        .filter { shouldFetch -> shouldFetch }
        .mapLatest {
            val breachesResponse = remote.getAllBreaches(userId)

            runCatching {
                val showMessage = internalSettings.getDarkWebAliasMessageVisibility().first()
                if (showMessage == Show && !breachesResponse.breaches.hasCustomDomains) {
                    internalSettings.setDarkWebAliasMessageVisibility(Dismissed)
                }
            }

            breachesResponse.toDomain()
        }
        .distinctUntilChanged()

    override fun observeCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<BreachEmailReport.Custom> =
        localBreachesDataSource
            .observeCustomEmail(userId, customEmailId)
            .onStart { refreshCustomEmailsIfNeeded(userId, customEmailId) }
            .map { it.toCustomReport() }

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> =
        localBreachesDataSource.observeCustomEmails()
            .onStart { refreshCustomEmails(userId) }

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
        runCatching {
            remote.verifyCustomEmail(userId, emailId, code)
        }.onSuccess {
            PassLogger.i(TAG, "Custom email verified successfully")
            localBreachesDataSource.getCustomEmail(userId, emailId)
                .copy(verified = true)
                .also { verifiedCustomEmail ->
                    localBreachesDataSource.upsertCustomEmail(userId, verifiedCustomEmail)
                }
                .also { refreshFlow.update { true } }
        }.onFailure { error ->
            PassLogger.w(TAG, "Error verifying custom email")
            PassLogger.w(TAG, error)
            when (error) {
                is CustomEmailDoesNotExistException -> {
                    localBreachesDataSource.deleteCustomEmail(userId, emailId)
                    refreshFlow.update { true }
                }

                else -> throw error
            }
        }
    }

    override fun observeAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<BreachEmailReport.Alias> = combine(
        observeItemById(aliasEmailId.shareId, aliasEmailId.itemId),
        localBreachesDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
            .onStart { refreshAliasEmailBreachesIfNeeded(userId, aliasEmailId) }
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
        localBreachesDataSource.observeProtonEmail(userId, addressId)
            .map { it.toProtonReport() }
            .onStart { refreshProtonEmailsIfNeeded(userId, addressId) }

    override fun observeProtonEmails(userId: UserId): Flow<List<BreachProtonEmail>> =
        localBreachesDataSource.observeProtonEmails(userId)
            .onStart { refreshProtonEmails(userId) }

    override fun observeBreachesForCustomEmail(userId: UserId, customEmailId: CustomEmailId): Flow<List<BreachEmail>> =
        localBreachesDataSource.observeCustomEmailBreaches(userId, customEmailId)
            .onStart { refreshCustomEmailBreachesIfNeeded(userId, customEmailId) }

    override fun observeBreachesForProtonEmail(userId: UserId, addressId: AddressId): Flow<List<BreachEmail>> =
        localBreachesDataSource.observeProtonEmailBreaches(userId, addressId)
            .onStart { refreshProtonEmailBreachesIfNeeded(userId, addressId) }

    override fun observeBreachesForAliasEmail(userId: UserId, aliasEmailId: AliasEmailId): Flow<List<BreachEmail>> =
        localBreachesDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
            .onStart { refreshAliasEmailBreachesIfNeeded(userId, aliasEmailId) }

    override suspend fun markProtonEmailAsResolved(userId: UserId, id: AddressId) {
        remote.markProtonEmailAsResolved(userId, id)

        localBreachesDataSource.getProtonEmail(userId, id)
            .copy(flags = BREACH_EMAIL_RESOLVED_STATE_VALUE)
            .also { verifiedProtonEmail ->
                localBreachesDataSource.upsertProtonEmail(userId, verifiedProtonEmail)
            }

        localBreachesDataSource.observeProtonEmailBreaches(userId, id)
            .first()
            .map { protonEmailBreach -> protonEmailBreach.copy(isResolved = true) }
            .also { resolvedProtonEmailBreaches ->
                localBreachesDataSource.upsertProtonEmailBreaches(
                    userId = userId,
                    addressId = id,
                    protonEmailBreaches = resolvedProtonEmailBreaches
                )
            }
    }

    override suspend fun markAliasEmailAsResolved(userId: UserId, aliasEmailId: AliasEmailId) {
        remote.markAliasEmailAsResolved(userId, aliasEmailId.shareId, aliasEmailId.itemId)

        localBreachesDataSource.observeAliasEmailBreaches(userId, aliasEmailId)
            .first()
            .map { aliasEmailBreach -> aliasEmailBreach.copy(isResolved = true) }
            .also { resolvedAliasEmailBreach ->
                localBreachesDataSource.upsertAliasEmailBreaches(
                    userId = userId,
                    aliasEmailId = aliasEmailId,
                    aliasEmailBreaches = resolvedAliasEmailBreach
                )
            }
    }

    override suspend fun markCustomEmailAsResolved(userId: UserId, id: CustomEmailId) {
        remote.markCustomEmailAsResolved(userId, id)
            .also { response ->
                localBreachesDataSource.upsertCustomEmail(userId, response.email.toDomain())
            }

        localBreachesDataSource.observeCustomEmailBreaches(userId, id)
            .first()
            .map { customEmailBreach -> customEmailBreach.copy(isResolved = true) }
            .also { resolvedCustomEmailBreaches ->
                localBreachesDataSource.upsertCustomEmailBreaches(
                    userId = userId,
                    customEmailId = id,
                    customEmailBreaches = resolvedCustomEmailBreaches
                )
            }
    }

    override suspend fun resendVerificationCode(userId: UserId, id: CustomEmailId) {
        remote.resendVerificationCode(userId, id)
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

        localBreachesDataSource.getProtonEmail(userId, addressId)
            .let { breachProtonEmail ->
                breachProtonEmail.copy(
                    flags = breachProtonEmail.flags xor EmailFlag.MonitoringDisabled.value
                )
            }
            .also { flaggedBreachProtonEmail ->
                localBreachesDataSource.upsertProtonEmail(userId, flaggedBreachProtonEmail)
            }
    }

    private suspend fun refreshProtonEmailBreachesIfNeeded(userId: UserId, id: AddressId) {
        runCatching { localBreachesDataSource.getProtonEmailBreaches(userId, id) }
            .onFailure { refreshProtonEmailBreaches(userId, id) }
    }

    private suspend fun refreshProtonEmailBreaches(userId: UserId, id: AddressId) {
        remote.getBreachesForProtonEmail(userId, id)
            .toDomain { breachDto -> BreachEmailId.Proton(BreachId(breachDto.id), id) }
            .also { protonEmailBreaches ->
                localBreachesDataSource.upsertProtonEmailBreaches(
                    userId = userId,
                    addressId = id,
                    protonEmailBreaches = protonEmailBreaches
                )
            }
    }

    private suspend fun refreshProtonEmails(userId: UserId) {
        observeAllBreaches(userId)
            .first()
            .breachedProtonEmails
            .also { protonEmails ->
                localBreachesDataSource.upsertProtonEmails(userId, protonEmails)
            }
    }

    private suspend fun refreshCustomEmailsIfNeeded(userId: UserId, customEmailId: CustomEmailId) {
        runCatching { localBreachesDataSource.getCustomEmail(userId, customEmailId) }
            .onFailure { refreshCustomEmails(userId) }
    }

    private suspend fun refreshCustomEmails(userId: UserId) {
        remote.getCustomEmails(userId)
            .emails
            .customEmails
            .map { customEmailDto -> customEmailDto.toDomain() }
            .also { customEmails ->
                localBreachesDataSource.upsertCustomEmails(userId, customEmails)
            }
    }

    private suspend fun refreshCustomEmailBreachesIfNeeded(userId: UserId, customEmailId: CustomEmailId) {
        runCatching { localBreachesDataSource.getCustomEmailBreaches(userId, customEmailId) }
            .onFailure { refreshCustomEmailBreaches(userId, customEmailId) }
    }

    private suspend fun refreshCustomEmailBreaches(userId: UserId, customEmailId: CustomEmailId) {
        remote.getBreachesForCustomEmail(userId, customEmailId)
            .toDomain { breachDto ->
                BreachEmailId.Custom(
                    id = BreachId(breachDto.id),
                    customEmailId = customEmailId
                )
            }
            .also { customEmailBreaches ->
                localBreachesDataSource.upsertCustomEmailBreaches(
                    userId = userId,
                    customEmailId = customEmailId,
                    customEmailBreaches = customEmailBreaches
                )
            }
    }

    private suspend fun refreshProtonEmailsIfNeeded(userId: UserId, addressId: AddressId) {
        runCatching { localBreachesDataSource.getProtonEmail(userId, addressId) }
            .onFailure { refreshProtonEmails(userId) }
    }

    private suspend fun refreshAliasEmailBreachesIfNeeded(userId: UserId, aliasEmailId: AliasEmailId) {
        runCatching { localBreachesDataSource.getAliasEmailBreaches(userId, aliasEmailId) }
            .onFailure { refreshAliasEmailBreaches(userId, aliasEmailId) }
    }

    private suspend fun refreshAliasEmailBreaches(userId: UserId, aliasEmailId: AliasEmailId) {
        remote.getBreachesForAliasEmail(userId, aliasEmailId.shareId, aliasEmailId.itemId)
            .toDomain { breach ->
                BreachEmailId.Alias(
                    BreachId(breach.id),
                    aliasEmailId.shareId,
                    aliasEmailId.itemId
                )
            }
            .also { aliasEmailBreaches ->
                localBreachesDataSource.upsertAliasEmailBreaches(
                    userId = userId,
                    aliasEmailId = aliasEmailId,
                    aliasEmailBreaches = aliasEmailBreaches
                )
            }
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
            breachedProtonEmails = protonEmails.map { protonEmail -> protonEmail.toDomain() },
            breachedAliases = emptyList()
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
