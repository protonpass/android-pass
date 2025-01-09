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

package proton.android.pass.data.api.usecases.capabilities

import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault

sealed interface CanShareVaultStatus {

    fun value(): Boolean

    data class CanShare(val invitesRemaining: Int) : CanShareVaultStatus {
        override fun value() = true
    }
    data class CannotShare(val reason: CannotShareReason) : CanShareVaultStatus {
        override fun value() = false
    }

    sealed interface CannotShareReason {
        data object NotEnoughPermissions : CannotShareReason
        data object NotEnoughInvites : CannotShareReason
        data object ItemInTrash : CannotShareReason
        data object Unknown : CannotShareReason
    }
}

interface CanShareVault {

    suspend operator fun invoke(shareId: ShareId): CanShareVaultStatus

    suspend operator fun invoke(vault: Vault): CanShareVaultStatus

}
