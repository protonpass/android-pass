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

package proton.android.pass.features.itemcreate.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.features.itemcreate.common.UIHiddenState

private const val PASSWORD_EMPTY = ""
private const val PASSWORD_ENCRYPTED = "Encrypted password"
private const val PASSWORD_PLAIN = "Plain password"

class PasswordInputPreviewProvider : PreviewParameterProvider<PasswordInputPreviewParams> {

    override val values: Sequence<PasswordInputPreviewParams>
        get() = sequenceOf(
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_EMPTY, PASSWORD_EMPTY),
                passwordStrength = PasswordStrength.None,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Strong,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Vulnerable,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Weak,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_EMPTY, PASSWORD_EMPTY),
                passwordStrength = PasswordStrength.None,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Strong,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Vulnerable,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Revealed(PASSWORD_ENCRYPTED, PASSWORD_PLAIN),
                passwordStrength = PasswordStrength.Weak,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Strong,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Vulnerable,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Weak,
                isEditAllowed = false
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Strong,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Vulnerable,
                isEditAllowed = true
            ),
            PasswordInputPreviewParams(
                hiddenState = UIHiddenState.Concealed(PASSWORD_ENCRYPTED),
                passwordStrength = PasswordStrength.Weak,
                isEditAllowed = true
            )
        )

}

data class PasswordInputPreviewParams(
    internal val hiddenState: UIHiddenState,
    internal val passwordStrength: PasswordStrength,
    internal val isEditAllowed: Boolean
)
