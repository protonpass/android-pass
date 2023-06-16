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

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R

class ProtonTextFieldPreviewProvider : PreviewParameterProvider<ProtonTextFieldPreviewData> {
    override val values: Sequence<ProtonTextFieldPreviewData>
        get() = sequenceOf(
            ProtonTextFieldPreviewData(value = "", placeholder = ""),
            ProtonTextFieldPreviewData(
                value = "",
                placeholder = "Name"
            ),
            ProtonTextFieldPreviewData(value = "", isError = true, placeholder = ""),
            ProtonTextFieldPreviewData(
                value = "contents with error",
                isError = true,
                placeholder = ""
            ),
            ProtonTextFieldPreviewData(
                value = "not editable",
                isEditable = false,
                placeholder = ""
            ),
            ProtonTextFieldPreviewData(
                value = "with icon",
                icon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_proton_minus_circle
                        ),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconNorm
                    )
                },
                placeholder = ""
            )
        )
}

data class ProtonTextFieldPreviewData(
    val value: String = "",
    val placeholder: String,
    val isError: Boolean = false,
    val isEditable: Boolean = true,
    val icon: (@Composable () -> Unit)? = null
)
