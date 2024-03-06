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

package proton.android.pass.data.api.usecases

import proton.android.pass.domain.ShareId

sealed interface CanAddressesBeInvitedResult {
    @JvmInline
    value class All(val addresses: List<String>) : CanAddressesBeInvitedResult

    data class Some(
        val canBe: List<String>,
        val cannotBe: List<String>
    ) : CanAddressesBeInvitedResult

    object None : CanAddressesBeInvitedResult
}

interface CheckCanAddressesBeInvited {
    suspend operator fun invoke(shareId: ShareId, addresses: List<String>): CanAddressesBeInvitedResult
}
