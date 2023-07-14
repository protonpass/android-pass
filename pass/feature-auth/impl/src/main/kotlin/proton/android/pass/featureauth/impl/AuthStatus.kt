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

package proton.android.pass.featureauth.impl

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface AuthEvent {
    object Success : AuthEvent
    object Failed : AuthEvent
    object Canceled : AuthEvent
    object SignOut : AuthEvent
    object ForceSignOut : AuthEvent
    object EnterPin : AuthEvent
    object Unknown : AuthEvent
}

sealed interface AuthError {
    @JvmInline
    value class WrongPassword(val remainingAttempts: Int) : AuthError
    object UnknownError : AuthError
}

sealed interface PasswordError {
    object EmptyPassword : PasswordError
}

data class AuthContent(
    val password: String,
    val address: String,
    val isLoadingState: IsLoadingState,
    val isPasswordVisible: Boolean,
    val error: Option<AuthError>,
    val passwordError: Option<PasswordError>
) {
    companion object {
        fun default(address: String) = AuthContent(
            password = "",
            address = address,
            isLoadingState = IsLoadingState.NotLoading,
            isPasswordVisible = false,
            error = None,
            passwordError = None
        )
    }
}

@Stable
data class AuthState(
    val event: AuthEvent,
    val content: AuthContent
) {
    companion object {
        val Initial = AuthState(
            event = AuthEvent.Unknown,
            content = AuthContent.default("")
        )
    }
}
