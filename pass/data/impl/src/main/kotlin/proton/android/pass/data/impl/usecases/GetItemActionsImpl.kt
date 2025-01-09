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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemActionsImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val observeShare: ObserveShare,
    private val observeUserPlan: GetUserPlan,
    private val observeAllShares: ObserveAllShares,
    private val canShareShare: CanShareShare
) : GetItemActions {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): ItemActions = combine(
        oneShot { observeShare(shareId).first() },
        oneShot { getItemById(shareId, itemId) },
        observeUserPlan(),
        observeAllShares()
    ) { share, item, userPlan, shares ->
        val isItemTrashed = item.state == ItemState.Trashed.value

        ItemActions(
            canShare = canShare(isItemTrashed, share),
            canEdit = canEdit(isItemTrashed, share, userPlan),
            canMoveToOtherVault = canMigrate(isItemTrashed, share, shares),
            canMoveToTrash = !isItemTrashed && share.canBeTrashed,
            canDelete = isItemTrashed && share.canBeDeleted,
            canRestoreFromTrash = isItemTrashed
        )
    }.first()

    private suspend fun canShare(isItemTrashed: Boolean, share: Share) = when {
        isItemTrashed -> {
            CanShareShareStatus.CannotShare(
                reason = CanShareShareStatus.CannotShareReason.ItemInTrash
            )
        }

        else -> {
            canShareShare(share.id)
        }
    }

    private fun canEdit(
        isItemTrashed: Boolean,
        share: Share,
        userPlan: Plan
    ) = when {
        isItemTrashed -> {
            ItemActions.CanEditActionState.Disabled(
                reason = ItemActions.CanEditActionState.CanEditDisabledReason.ItemInTrash
            )
        }

        share.canBeUpdated -> {
            ItemActions.CanEditActionState.Enabled
        }

        share.isOwner && userPlan.planType is PlanType.Free -> {
            ItemActions.CanEditActionState.Disabled(
                reason = ItemActions.CanEditActionState.CanEditDisabledReason.Downgraded
            )
        }

        else -> {
            ItemActions.CanEditActionState.Disabled(
                reason = ItemActions.CanEditActionState.CanEditDisabledReason.NotEnoughPermission
            )
        }
    }

    private fun canMigrate(
        isItemTrashed: Boolean,
        share: Share,
        shares: List<Share>
    ) = when {
        isItemTrashed -> {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                reason = ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.ItemInTrash
            )
        }

        shares.none { it.canBeCreated && it.id != share.id } -> {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                reason = ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
            )
        }

        share.isOwner -> {
            ItemActions.CanMoveToOtherVaultState.Enabled
        }

        share.canBeDeleted -> {
            ItemActions.CanMoveToOtherVaultState.Enabled
        }

        else -> {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                reason = ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NotEnoughPermission
            )
        }
    }
}
