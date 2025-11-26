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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.usecases.CanAddressesBeInvitedResult
import proton.android.pass.data.api.usecases.CheckCanAddressesBeInvited
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.requests.CheckAddressesCanBeInvitedRequest
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.domain.OrganizationShareMode
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class CheckCanAddressesBeInvitedImpl @Inject constructor(
    private val observeOrganizationSettings: ObserveOrganizationSettings,
    private val api: ApiProvider,
    private val accountManager: AccountManager
) : CheckCanAddressesBeInvited {

    override suspend fun invoke(shareId: ShareId, addresses: List<String>): CanAddressesBeInvitedResult {
        val settingsOption = observeOrganizationSettings().firstOrNull() ?: run {
            PassLogger.w(TAG, "Organization settings not available")
            return CanAddressesBeInvitedResult.None(
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.Unknown
            )
        }

        if (addresses.isEmpty()) {
            return CanAddressesBeInvitedResult.None(
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.Empty
            )
        }

        return when (settingsOption) {
            None -> CanAddressesBeInvitedResult.None(
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.Unknown
            )

            is Some -> when (val settings = settingsOption.value) {
                OrganizationSettings.NotAnOrganization ->
                    CanAddressesBeInvitedResult.All(addresses)
                is OrganizationSettings.Organization -> when (settings.shareMode) {
                    OrganizationShareMode.Unrestricted ->
                        CanAddressesBeInvitedResult.All(addresses)
                    OrganizationShareMode.OrganizationOnly ->
                        checkAddressesCanBeInvited(shareId, addresses)
                }
            }
        }
    }

    private suspend fun checkAddressesCanBeInvited(
        shareId: ShareId,
        addresses: List<String>
    ): CanAddressesBeInvitedResult {
        val userId = accountManager.getPrimaryUserId().filterNotNull().first()
        val results = coroutineScope {
            val chunks = addresses.chunked(MAX_ADDRESSES_PER_CHUNK)
            chunks.map { chunk ->
                async {
                    processAddresses(userId, shareId, chunk)
                }
            }.awaitAll()
        }.transpose()

        val successes = results.getOrElse {
            PassLogger.w(TAG, "Error checking if addresses can be invited")
            PassLogger.w(TAG, it)
            return CanAddressesBeInvitedResult.None(
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.Unknown
            )
        }

        val aggregated = successes.fold(
            initial = ProcessResult(emptyList(), emptyList())
        ) { acc, result ->
            ProcessResult(
                can = acc.can + result.can,
                cannot = acc.cannot + result.cannot
            )
        }

        return when {
            aggregated.cannot.isEmpty() -> CanAddressesBeInvitedResult.All(addresses)
            aggregated.can.isEmpty() -> CanAddressesBeInvitedResult.None(
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.CannotInviteOutsideOrg
            )

            else -> CanAddressesBeInvitedResult.Some(
                canBe = aggregated.can,
                cannotBe = aggregated.cannot,
                reason = CanAddressesBeInvitedResult.CannotInviteAddressReason.CannotInviteOutsideOrg
            )
        }
    }

    private suspend fun processAddresses(
        userId: UserId,
        shareId: ShareId,
        addresses: List<String>
    ): Result<ProcessResult> {
        val response = api.get<PasswordManagerApi>(userId).invoke {
            checkAddressesCanBeInvited(
                shareId = shareId.id,
                request = CheckAddressesCanBeInvitedRequest(addresses)
            )
        }

        return when (response) {
            is ApiResult.Success -> {
                val result = ProcessResult(
                    can = response.value.emails,
                    cannot = addresses - response.value.emails.toSet()
                )

                Result.success(result)
            }

            is ApiResult.Error -> {
                Result.failure(
                    response.cause ?: RuntimeException("Error checking if addresses can be invited")
                )
            }
        }
    }

    private data class ProcessResult(
        val can: List<String>,
        val cannot: List<String>
    )

    companion object {
        private const val TAG = "CheckCanAddressesBeInvitedImpl"
        private const val MAX_ADDRESSES_PER_CHUNK = 10
    }
}
