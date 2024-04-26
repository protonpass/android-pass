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

package proton.android.pass.features.security.center.darkweb.presentation.customemails

import androidx.compose.runtime.Stable
import proton.android.pass.domain.breach.CustomEmailId

@Stable
internal sealed interface UnverifiedCustomEmailOptionsEvent {
    @Stable
    data object Idle : UnverifiedCustomEmailOptionsEvent

    @Stable
    data object Close : UnverifiedCustomEmailOptionsEvent

    @Stable
    data class Verify(
        val id: CustomEmailId,
        val email: String
    ) : UnverifiedCustomEmailOptionsEvent
}

internal enum class UnverifiedCustomEmailOptionsLoadingState {
    Idle,
    Verify,
    Remove
}

@Stable
internal data class UnverifiedCustomEmailState(
    val event: UnverifiedCustomEmailOptionsEvent,
    val loadingState: UnverifiedCustomEmailOptionsLoadingState
) {
    companion object {
        val Initial = UnverifiedCustomEmailState(
            event = UnverifiedCustomEmailOptionsEvent.Idle,
            loadingState = UnverifiedCustomEmailOptionsLoadingState.Idle
        )
    }
}
