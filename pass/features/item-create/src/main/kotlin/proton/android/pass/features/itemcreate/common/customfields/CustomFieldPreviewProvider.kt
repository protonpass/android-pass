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

package proton.android.pass.features.itemcreate.common.customfields

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider

internal class ThemeCustomFieldPreviewProvider : ThemePairPreviewProvider<CustomFieldInput>(
    CustomFieldPreviewProvider()
)

internal class CustomFieldPreviewProvider : PreviewParameterProvider<CustomFieldInput> {

    override val values: Sequence<CustomFieldInput> = sequence {
        for (text in listOf("", "value")) {
            for (enabled in listOf(true, false)) {
                for (showLeadingIcon in listOf(true, false)) {
                    CustomFieldInput(
                        text = text,
                        enabled = enabled,
                        showLeadingIcon = showLeadingIcon
                    ).also { customFieldInput ->
                        yield(customFieldInput)
                    }
                }
            }
        }
    }
}

internal data class CustomFieldInput(
    val text: String,
    val enabled: Boolean,
    val showLeadingIcon: Boolean
)
