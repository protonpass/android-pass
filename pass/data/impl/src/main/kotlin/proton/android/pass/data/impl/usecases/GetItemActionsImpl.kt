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

import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanShareVault
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemState
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.canCreate
import proton.pass.domain.canDelete
import proton.pass.domain.canTrash
import proton.pass.domain.canUpdate
import proton.pass.domain.toPermissions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetItemActionsImpl @Inject constructor(
    private val getItemById: GetItemById,
    private val observeUserPlan: GetUserPlan,
    private val canShareVault: CanShareVault,
    private val observeVaults: ObserveVaults,
) : GetItemActions {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): ItemActions {
        val item = getItemById(shareId, itemId).firstOrNull()
            ?: throw IllegalStateException("Item not found")

        val vaults = observeVaults().firstOrNull()
            ?: throw IllegalStateException("Could not fetch vaults")

        val vault = vaults.firstOrNull { it.shareId == shareId }
            ?: throw IllegalStateException("Could not find vault")

        val userPlan = observeUserPlan().firstOrNull()
            ?: throw IllegalStateException("Could not get user plan")

        return getItemActions(
            item = item,
            vault = vault,
            vaults = vaults,
            userPlan = userPlan
        )
    }

    private suspend fun getItemActions(
        item: Item,
        vault: Vault,
        vaults: List<Vault>,
        userPlan: Plan
    ): ItemActions {
        val permissions = vault.role.toPermissions()

        val isTrashed = item.state == ItemState.Trashed.value
        val canShare = canShareVault(vault)
        val canMoveToTrash = !isTrashed && permissions.canTrash()
        val canDelete = isTrashed && permissions.canDelete()

        val canEdit = getCanEdit(item, userPlan, vault)
        val canMoveToOtherVault = getCanMoveToOtherVault(
            item = item,
            vault = vault,
            vaults = vaults
        )

        return ItemActions(
            canShare = canShare,
            canMoveToTrash = canMoveToTrash,
            canDelete = canDelete,
            canEdit = canEdit,
            canMoveToOtherVault = canMoveToOtherVault,
            canRestoreFromTrash = false
        )
    }

    private fun getCanEdit(
        item: Item,
        userPlan: Plan,
        vault: Vault
    ): ItemActions.CanEditActionState {
        val permissions = vault.role.toPermissions()
        val isTrashed = item.state == ItemState.Trashed.value
        if (isTrashed) {
            return ItemActions.CanEditActionState.Disabled(
                ItemActions.CanEditActionState.CanEditDisabledReason.ItemInTrash
            )
        }

        val canUpdate = permissions.canUpdate()
        return if (!canUpdate) {
            if (userPlan.planType is PlanType.Free && vault.isOwned) {
                // User is in downgraded mode
                ItemActions.CanEditActionState.Disabled(ItemActions.CanEditActionState.CanEditDisabledReason.Downgraded)
            } else {
                ItemActions.CanEditActionState.Disabled(
                    ItemActions.CanEditActionState.CanEditDisabledReason.NotEnoughPermission
                )
            }
        } else {
            ItemActions.CanEditActionState.Enabled
        }
    }

    private fun getCanMoveToOtherVault(
        item: Item,
        vault: Vault,
        vaults: List<Vault>
    ): ItemActions.CanMoveToOtherVaultState {
        val isTrashed = item.state == ItemState.Trashed.value
        if (isTrashed) {
            return ItemActions.CanMoveToOtherVaultState.Disabled(
                ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.ItemInTrash
            )
        }

        val isThereAnotherVault = vaults.any { other ->
            other.shareId != vault.shareId && other.role.toPermissions().canCreate()
        }
        if (!isThereAnotherVault) {
            return ItemActions.CanMoveToOtherVaultState.Disabled(
                ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NoVaultToMoveToAvailable
            )
        }

        val permissions = vault.role.toPermissions()
        val canDelete = permissions.canDelete()

        return if (canDelete) {
            ItemActions.CanMoveToOtherVaultState.Enabled
        } else {
            ItemActions.CanMoveToOtherVaultState.Disabled(
                ItemActions.CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NotEnoughPermission
            )
        }
    }
}
