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

package proton.android.pass.features.credentials.passkeys.selection.presentation

import androidx.compose.runtime.Immutable
import proton.android.pass.preferences.ThemePreference

@Immutable
internal sealed interface PasskeyCredentialSelectionState {

    @Immutable
    data object Close : PasskeyCredentialSelectionState

    @Immutable
    data object NotReady : PasskeyCredentialSelectionState

    @Immutable
    data class Ready(
        internal val themePreference: ThemePreference,
        internal val isBiometricAuthRequired: Boolean,
        internal val request: PasskeyCredentialSelectionRequest,
        internal val event: PasskeyCredentialSelectionStateEvent
    ) : PasskeyCredentialSelectionState {

        internal val actionAfterAuth: PasskeyCredentialSelectionActionAfterAuth = when (request) {
            is PasskeyCredentialSelectionRequest.Select -> {
                PasskeyCredentialSelectionActionAfterAuth.SelectItem
            }

            is PasskeyCredentialSelectionRequest.Use -> {
                PasskeyCredentialSelectionActionAfterAuth.EmitEvent
            }
        }

    }

}
