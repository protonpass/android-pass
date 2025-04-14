/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passwords.creation.presentation

import androidx.compose.runtime.Immutable
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.features.itemcreate.login.InitialCreateLoginUiState
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.preferences.ThemePreference

@Immutable
internal sealed interface PasswordCredentialCreationState {

    @Immutable
    data object Close : PasswordCredentialCreationState

    @Immutable
    data object NotReady : PasswordCredentialCreationState

    @Immutable
    data class Ready(
        internal val request: PasswordCredentialCreationRequest,
        internal val themePreference: ThemePreference,
        internal val isBiometricAuthRequired: Boolean,
        internal val event: PasswordCredentialCreationStateEvent
    ) : PasswordCredentialCreationState {

        internal val initialCreateLoginUiState: InitialCreateLoginUiState = InitialCreateLoginUiState(
            title = request.domain,
            username = request.id,
            password = request.password,
            url = request.domain
        )

        internal val selectItemState: SelectItemState = SelectItemState.Passkey.Register(
            title = request.domain,
            suggestion = Suggestion.Url(request.domain)
        )

    }

}
