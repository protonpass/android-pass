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

package proton.android.pass.features.extrapassword.auth.presentation

import androidx.compose.runtime.Stable
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Stable
internal sealed interface ExtraPasswordError {
    @Stable
    data object EmptyPassword : ExtraPasswordError

    @Stable
    data object WrongPassword : ExtraPasswordError
}

@Stable
internal sealed interface EnterExtraPasswordEvent {
    @Stable
    data object Success : EnterExtraPasswordEvent

    @Stable
    @JvmInline
    value class Logout(val userId: UserId) : EnterExtraPasswordEvent

    @Stable
    data object Idle : EnterExtraPasswordEvent
}

@Stable
internal data class ExtraPasswordState(
    val email: String,
    val event: EnterExtraPasswordEvent,
    val loadingState: IsLoadingState,
    val error: Option<ExtraPasswordError>
) {

    internal val isError: Boolean = error.isNotEmpty()

    companion object {
        val Initial = ExtraPasswordState(
            email = "",
            event = EnterExtraPasswordEvent.Idle,
            loadingState = IsLoadingState.NotLoading,
            error = None
        )
    }
}
