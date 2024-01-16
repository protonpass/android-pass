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

package proton.android.pass.featuresharing.impl.sharingpermissions.bottomsheet

import androidx.compose.runtime.Immutable
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType

@Immutable
sealed interface SharingPermissionsBottomSheetEvent {
    @Immutable
    object Unknown : SharingPermissionsBottomSheetEvent

    @Immutable
    object Close : SharingPermissionsBottomSheetEvent
}

@Immutable
sealed interface SharingPermissionsEditMode {
    @Immutable
    object All : SharingPermissionsEditMode

    @Immutable
    data class EditOne(val email: String, val sharingType: SharingType) : SharingPermissionsEditMode
}

@Immutable
data class SharingPermissionsBottomSheetUiState(
    val event: SharingPermissionsBottomSheetEvent,
    val displayRemove: Boolean,
    val mode: SharingPermissionsEditMode
) {
    companion object {
        fun Initial(mode: SharingPermissionsEditMode) = SharingPermissionsBottomSheetUiState(
            event = SharingPermissionsBottomSheetEvent.Unknown,
            mode = mode,
            displayRemove = false
        )
    }
}
