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

package proton.android.pass.composecomponents.impl.form

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider

class ProtonSelectableTextFieldPreviewProvider :
    PreviewParameterProvider<ProtonSelectableTextFieldPreviewParams> {

    override val values: Sequence<ProtonSelectableTextFieldPreviewParams>
        get() = sequenceOf(
            ProtonSelectableTextFieldPreviewParams(),
            ProtonSelectableTextFieldPreviewParams(
                text = "Example input text enabled",
            ),
            ProtonSelectableTextFieldPreviewParams(
                text = "Example input text disabled",
                isEnabled = false,
            ),
            ProtonSelectableTextFieldPreviewParams(
                text = "Example input text with error",
                errorText = "Example error message"
            ),
        )
}

class ThemedProtonTextFieldPreviewProvider :
    ThemePairPreviewProvider<ProtonSelectableTextFieldPreviewParams>(
        provider = ProtonSelectableTextFieldPreviewProvider(),
    )

data class ProtonSelectableTextFieldPreviewParams(
    val text: String = "",
    val errorText: String? = null,
    val isEnabled: Boolean = true,
)
