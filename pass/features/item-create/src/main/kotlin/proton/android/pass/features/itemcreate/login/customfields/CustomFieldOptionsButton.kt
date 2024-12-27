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

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.R as CoreR

@Composable
fun CustomFieldOptionsButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    CircleIconButton(
        modifier = modifier,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
            contentDescription = stringResource(R.string.custom_field_options_content_description),
            tint = PassTheme.colors.loginInteractionNormMajor2
        )
    }
}

@Preview
@Composable
fun CustomFieldOptionsButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CustomFieldOptionsButton(onClick = {})
        }
    }
}
