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

import proton.pass.domain.Vault

interface CanCreateItemInVault {
    suspend operator fun invoke(vault: Vault): CanCreateResult

    sealed interface CanCreateResult {
        object CanCreate : CanCreateResult
        data class CannotCreate(val reason: Reason) : CanCreateResult

        fun value(): Boolean = when (this) {
            is CanCreate -> true
            is CannotCreate -> false
        }

        sealed interface Reason {
            object NoCreatePermission : Reason
            object Downgraded : Reason
            object Unknown : Reason
        }
    }
}
