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

package proton.android.pass.features.sharing.sharingpermissions.bottomsheet

import androidx.compose.runtime.Immutable
import proton.android.pass.features.sharing.sharingpermissions.SharingType

@Immutable
internal sealed interface SharingPermissionsBottomSheetEvent {

    @Immutable
    data object Unknown : SharingPermissionsBottomSheetEvent

    @Immutable
    data object Close : SharingPermissionsBottomSheetEvent

}

@Immutable
internal sealed interface SharingPermissionsEditMode {

    @Immutable
    data object All : SharingPermissionsEditMode

    @Immutable
    data class EditOne(val email: String, val sharingType: SharingType) : SharingPermissionsEditMode

}

@Immutable
internal data class SharingPermissionsBottomSheetUiState(
    val event: SharingPermissionsBottomSheetEvent,
    val displayRemove: Boolean,
    val mode: SharingPermissionsEditMode
) {

    internal companion object {

        fun initial(mode: SharingPermissionsEditMode) = SharingPermissionsBottomSheetUiState(
            event = SharingPermissionsBottomSheetEvent.Unknown,
            mode = mode,
            displayRemove = false
        )

    }

}
