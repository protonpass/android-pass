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

package proton.android.pass.features.itemcreate.login.customfields

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.features.itemcreate.login.LoginItemValidationErrors

internal class ThemeTotpCustomFieldInput :
    ThemePairPreviewProvider<TotpCustomFieldInput>(TotpCustomFieldPreviewProvider())

internal class TotpCustomFieldPreviewProvider : PreviewParameterProvider<TotpCustomFieldInput> {

    override val values: Sequence<TotpCustomFieldInput> = sequence {
        for (text in listOf("", "mytotp")) {
            for (showLeadingIcon in listOf(true, false)) {
                for (isEnabled in listOf(true, false)) {
                    yield(TotpCustomFieldInput(text, isEnabled, null, showLeadingIcon))
                }
                yield(
                    TotpCustomFieldInput(
                        text,
                        false,
                        LoginItemValidationErrors.CustomFieldValidationError.EmptyField(1),
                        showLeadingIcon
                    )
                )
                yield(
                    TotpCustomFieldInput(
                        text,
                        false,
                        LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp(1),
                        showLeadingIcon
                    )
                )
            }
        }
    }
}

internal data class TotpCustomFieldInput(
    val text: String,
    val isEnabled: Boolean,
    val error: LoginItemValidationErrors.CustomFieldValidationError?,
    val showLeadingIcon: Boolean = true
)
