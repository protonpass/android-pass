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

package proton.android.pass.data.api.usecases

import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

data class ItemActions(
    val canShare: CanShareShareStatus,
    val canEdit: CanEditActionState,
    val canMoveToOtherVault: CanMoveToOtherVaultState,
    val canMoveToTrash: Boolean,
    val canRestoreFromTrash: Boolean,
    val canDelete: Boolean,
    val canResetHistory: Boolean
) {
    sealed interface CanEditActionState {

        fun value(): Boolean

        data object Enabled : CanEditActionState {
            override fun value() = true
        }

        @JvmInline
        value class Disabled(val reason: CanEditDisabledReason) : CanEditActionState {
            override fun value() = false
        }

        sealed interface CanEditDisabledReason {
            data object NotEnoughPermission : CanEditDisabledReason
            data object Downgraded : CanEditDisabledReason
            data object ItemInTrash : CanEditDisabledReason
        }
    }

    sealed interface CanMoveToOtherVaultState {

        fun value(): Boolean

        data object Enabled : CanMoveToOtherVaultState {
            override fun value() = true
        }

        @JvmInline
        value class Disabled(
            val reason: CanMoveToOtherVaultDisabledReason
        ) : CanMoveToOtherVaultState {
            override fun value() = false
        }

        sealed interface CanMoveToOtherVaultDisabledReason {
            data object NotEnoughPermission : CanMoveToOtherVaultDisabledReason
            data object NoVaultToMoveToAvailable : CanMoveToOtherVaultDisabledReason
            data object ItemInTrash : CanMoveToOtherVaultDisabledReason
        }
    }

    companion object {
        val Disabled = ItemActions(
            canShare = CanShareShareStatus.CannotShare(CanShareShareStatus.CannotShareReason.Unknown),
            canEdit = CanEditActionState.Disabled(CanEditActionState.CanEditDisabledReason.NotEnoughPermission),
            canMoveToOtherVault = CanMoveToOtherVaultState.Disabled(
                CanMoveToOtherVaultState.CanMoveToOtherVaultDisabledReason.NotEnoughPermission
            ),
            canMoveToTrash = false,
            canRestoreFromTrash = false,
            canDelete = false,
            canResetHistory = false
        )
    }
}

interface GetItemActions {
    suspend operator fun invoke(shareId: ShareId, itemId: ItemId): ItemActions
}
