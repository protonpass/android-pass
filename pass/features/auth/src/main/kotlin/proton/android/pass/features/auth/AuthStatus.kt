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
import kotlinx.collections.immutable.persistentMapOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

sealed interface AuthEvent {

    @JvmInline
    value class Success(val origin: AuthOrigin) : AuthEvent
    data object Failed : AuthEvent
    data object Canceled : AuthEvent

    @JvmInline
    value class SignOut(val userId: UserId) : AuthEvent

    @JvmInline
    value class ForceSignOut(val userId: UserId) : AuthEvent

    @JvmInline
    value class EnterPin(val origin: AuthOrigin) : AuthEvent
    data object EnterBiometrics : AuthEvent
    data object Unknown : AuthEvent
}

enum class AuthMethod {
    Pin,
    Fingerprint
}

sealed interface PasswordError {
    data object IncorrectPassword : PasswordError
    data object EmptyPassword : PasswordError
}

data class AuthStateContent(
    val userId: Option<UserId>,
    val password: String,
    val address: Option<String>,
    val isLoadingState: IsLoadingState,
    val isPasswordVisible: Boolean,
    val remainingPasswordAttempts: Option<Int>,
    val passwordError: Option<PasswordError>,
    val authMethod: Option<AuthMethod>,
    val showExtraPassword: LoadingResult<Boolean>,
    val showPinOrBiometry: Boolean,
    val showLogout: Boolean,
    val showBackNavigation: Boolean,
    val accountSwitcherState: AccountSwitcherState
) {
    companion object {
        fun default(address: Option<String>) = AuthStateContent(
            userId = None,
            password = "",
            address = address,
            isLoadingState = IsLoadingState.NotLoading,
            isPasswordVisible = false,
            remainingPasswordAttempts = None,
            passwordError = None,
            authMethod = None,
            accountSwitcherState = AccountSwitcherState(
                isAccountSwitchV1Enabled = false,
                accounts = persistentMapOf()
            ),
            showExtraPassword = LoadingResult.Loading,
            showPinOrBiometry = false,
            showLogout = true,
            showBackNavigation = false
        )
    }
}

@Stable
data class AuthState(
    val event: Option<AuthEvent>,
    val content: AuthStateContent
) {

    companion object {
        val Initial = AuthState(
            event = None,
            content = AuthStateContent.default(None)
        )
    }

}
