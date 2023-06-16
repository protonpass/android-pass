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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun BottomSheetTitle(
    modifier: Modifier = Modifier,
    title: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = PassTypography.body3Bold,
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
fun BottomSheetTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class)
    input: Boolean
) {
    PassTheme(isDark = input) {
        Surface {
            BottomSheetTitle(
                title = "Generate password"
            )
        }
    }
}
