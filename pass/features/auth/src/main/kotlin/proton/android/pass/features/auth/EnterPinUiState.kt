/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.auth

import androidx.compose.runtime.Stable
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Stable
internal sealed interface EnterPinUiState {

    @Stable
    data object NotInitialised : EnterPinUiState

    @Stable
    data class Data(
        val event: EnterPinEvent,
        val pinError: Option<PinError>,
        val isLoadingState: IsLoadingState,
        val userId: UserId
    ) : EnterPinUiState
}

internal sealed interface PinError {

    data object PinEmpty : PinError

    data class PinIncorrect(val remainingAttempts: Int) : PinError

}
