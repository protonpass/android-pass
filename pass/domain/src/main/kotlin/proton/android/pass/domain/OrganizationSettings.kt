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

package proton.android.pass.domain

enum class OrganizationShareMode(val value: Int) {
    Unrestricted(0),
    OrganizationOnly(1);

    companion object {
        fun fromValue(value: Int): OrganizationShareMode = entries
            .firstOrNull { it.value == value }
            ?: Unrestricted
    }
}

sealed class ForceLockSeconds {
    data object NotEnforced : ForceLockSeconds()
    data class Enforced(val value: Int) : ForceLockSeconds()

    companion object {
        fun fromValue(value: Int): ForceLockSeconds = if (value == 0) NotEnforced else Enforced(value)
    }
}

sealed interface OrganizationSettings {
    data object NotAnOrganization : OrganizationSettings
    data class Organization(
        val canUpdate: Boolean,
        val shareMode: OrganizationShareMode,
        val forceLockSeconds: ForceLockSeconds
    ) : OrganizationSettings

    fun isEnforced(): Boolean = when (this) {
        is Organization -> forceLockSeconds is ForceLockSeconds.Enforced
        else -> false
    }
}
