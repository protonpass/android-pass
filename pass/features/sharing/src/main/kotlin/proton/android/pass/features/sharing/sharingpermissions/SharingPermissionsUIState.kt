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

package proton.android.pass.features.sharing.sharingpermissions

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.features.sharing.common.AddressPermissionUiState

enum class SharingType {
    Read,
    Write,
    Admin
}

@Immutable
internal data class SharingPermissionsUIState(
    internal val addresses: ImmutableList<AddressPermissionUiState> = persistentListOf(),
    internal val vaultName: String? = null,
    internal val event: SharingPermissionsEvents = SharingPermissionsEvents.Idle
) {

    internal val memberCount: Int = addresses.size

}
