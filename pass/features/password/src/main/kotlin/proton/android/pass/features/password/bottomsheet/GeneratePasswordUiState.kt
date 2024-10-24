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

package proton.android.pass.features.password.bottomsheet

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.api.passwords.PasswordConfig

@Immutable
enum class GeneratePasswordMode {
    CopyAndClose,
    CancelConfirm
}

@Immutable
internal data class GeneratePasswordUiState(
    internal val password: String,
    internal val passwordStrength: PasswordStrength,
    internal val mode: GeneratePasswordMode,
    internal val passwordConfig: PasswordConfig?
) {

    internal companion object {

        internal fun initial(mode: GeneratePasswordMode) = GeneratePasswordUiState(
            password = "",
            passwordStrength = PasswordStrength.None,
            mode = mode,
            passwordConfig = null
        )

    }
}
