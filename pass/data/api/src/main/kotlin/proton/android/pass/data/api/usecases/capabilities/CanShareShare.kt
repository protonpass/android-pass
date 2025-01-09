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

sealed interface CanShareShareStatus {

    val value: Boolean

    data class CanShare(val invitesRemaining: Int) : CanShareShareStatus {

        override val value: Boolean = true

    }

    data class CannotShare(val reason: CannotShareReason) : CanShareShareStatus {

        override val value: Boolean = false

    }

    enum class CannotShareReason {
        ItemInTrash,
        NotEnoughInvites,
        NotEnoughPermissions,
        Unknown
    }
}

interface CanShareShare {

    suspend operator fun invoke(shareId: ShareId): CanShareShareStatus

}
