/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class CanShareShareImpl @Inject constructor(
    private val getShareById: GetShareById,
    private val getUserPlan: GetUserPlan,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository
) : CanShareShare {

    override suspend fun invoke(shareId: ShareId): CanShareShareStatus {
        val share = runCatching { getShareById(shareId = shareId) }.getOrElse { error ->
            PassLogger.w(TAG, "There was an error getting the share")
            PassLogger.w(TAG, error)

            return CanShareShareStatus.CannotShare(CanShareShareStatus.CannotShareReason.Unknown)
        }

        return when (share) {
            is Share.Item -> getItemCanShareStatus(share)
            is Share.Vault -> getVaultCanShareStatus(share)
        }
    }

    private suspend fun getItemCanShareStatus(itemShare: Share.Item) = when {
        // If a user with FF disabled is invited to an item, it can still accept the invite and view the item,
        // but sharing is not allowed despite the permissions.
        !featureFlagsRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1).first() -> {
            CanShareShareStatus.CannotShare(
                reason = CanShareShareStatus.CannotShareReason.Unknown
            )
        }

        itemShare.canBeShared -> {
            CanShareShareStatus.CanShare(invitesRemaining = itemShare.remainingInvites)
        }

        else -> {
            CanShareShareStatus.CannotShare(
                reason = CanShareShareStatus.CannotShareReason.NotEnoughPermissions
            )
        }
    }

    private suspend fun getVaultCanShareStatus(vaultShare: Share.Vault) = when {
        getUserPlan().first().isBusinessPlan -> {
            CanShareShareStatus.CanShare(invitesRemaining = vaultShare.remainingInvites)
        }

        !vaultShare.hasRemainingInvites -> {
            CanShareShareStatus.CannotShare(
                reason = CanShareShareStatus.CannotShareReason.NotEnoughInvites
            )
        }

        vaultShare.isOwner -> {
            CanShareShareStatus.CanShare(invitesRemaining = vaultShare.remainingInvites)
        }

        vaultShare.isAdmin -> {
            CanShareShareStatus.CanShare(invitesRemaining = vaultShare.remainingInvites)
        }

        else -> {
            CanShareShareStatus.CannotShare(
                reason = CanShareShareStatus.CannotShareReason.NotEnoughPermissions
            )
        }
    }

    private companion object {

        private const val TAG = "CanShareVaultImpl"

    }

}
