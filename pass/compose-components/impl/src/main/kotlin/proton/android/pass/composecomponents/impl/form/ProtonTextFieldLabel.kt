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

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography

@Composable
fun ProtonTextFieldLabel(
    modifier: Modifier = Modifier,
    text: String,
    isError: Boolean = false
) {
    Text(
        modifier = modifier,
        text = text,
        color = if (isError) {
            PassTheme.colors.signalDanger
        } else {
            PassTheme.colors.textWeak
        },
        style = if (isError) {
            ProtonTheme.typography.defaultSmallWeak
        } else {
            PassTypography.body3Regular
        }
    )
}
