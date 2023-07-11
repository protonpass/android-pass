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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.ThemePairPreviewProvider

class MasterPasswordFormPreviewProvider : PreviewParameterProvider<MasterPasswordInput> {
    override val values: Sequence<MasterPasswordInput>
        get() = sequenceOf(
            MasterPasswordInput(),
            MasterPasswordInput("password", isPasswordVisible = true),
            MasterPasswordInput("password", isPasswordVisible = false),
            MasterPasswordInput(isPasswordVisible = false, error = AuthError.WrongPassword(2).some()),
        )
}

data class MasterPasswordInput(
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val error: Option<AuthError> = None,
)

class ThemeMasterPasswordPreviewProvider : ThemePairPreviewProvider<MasterPasswordInput>(
    MasterPasswordFormPreviewProvider()
)
