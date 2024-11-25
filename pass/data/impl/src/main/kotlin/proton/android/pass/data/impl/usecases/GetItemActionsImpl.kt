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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.android.pass.data.api.usecases.capabilities.CanShareVaultStatus
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.canDelete
import proton.android.pass.domain.canTrash
import proton.android.pass.domain.canUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemActionsImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val observeShare: ObserveShare,
    private val observeUserPlan: GetUserPlan,
    private val observeAllShares: ObserveAllShares,
    private val canShareVault: CanShareVault
) : GetItemActions {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): ItemActions = combine(
        observeShare(shareId),
        oneShot { getItemById(shareId, itemId) },
        observeUserPlan(),
        observeAllShares()
    ) { shareOption, item, userPlan, shares ->
        when (shareOption) {
            None -> throw IllegalStateException("Share not found")
            is Some -> shareOption.value.let { share ->
                val isItemTrashed = item.state == ItemState.Trashed.value

                ItemActions(
                    canShare = canShare(isItemTrashed, share),
                    canEdit = canEdit(isItemTrashed, share, userPlan),
                    canMoveToOtherVault = canMigrate(isItemTrashed, share, shares),
                    canMoveToTrash = !isItemTrashed && share.permission.canTrash(),
                    canDelete = isItemTrashed && share.permission.canDelete(),
                    canRestoreFromTrash = false
                )
            }
        }
    }.first()

    private suspend fun canShare(isItemTrashed: Boolean, share: Share) = if (isItemTrashed) {
        CanShareVaultStatus.CannotShare(
            reason = CanShareVaultStatus.CannotShareReason.ItemInTrash
        )
    } else {
        canShareVault(share.id)
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

        share.permission.canUpdate() -> {
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

        shares.none { it.permission.canCreate() && it.id != share.id } -> {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                reason = ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
            )
        }

        share.isOwner -> {
            ItemActions.CanMoveToOtherVaultState.Enabled
        }

        share.permission.canDelete() -> {
            ItemActions.CanMoveToOtherVaultState.Enabled
        }

        else -> {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                reason = ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NotEnoughPermission
            )
        }
    }

}
