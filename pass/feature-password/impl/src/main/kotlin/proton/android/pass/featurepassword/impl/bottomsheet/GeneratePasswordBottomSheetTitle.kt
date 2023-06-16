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

package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.featurepassword.R

@Composable
fun GeneratePasswordBottomSheetTitle(
    modifier: Modifier = Modifier,
    onRegenerate: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.bottomsheet_generate_password_title),
            style = PassTypography.body3Bold,
            fontSize = 16.sp
        )
        CircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
            onClick = { onRegenerate() }
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrows_rotate),
                contentDescription = stringResource(R.string.regenerate_password_icon_content_description),
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
        }
    }
}

@Preview
@Composable
fun GeneratePasswordBottomSheetTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordBottomSheetTitle(onRegenerate = {})
        }
    }
}
