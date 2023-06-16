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

package proton.android.pass.composecomponents.impl.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.placeholder

@Composable
fun SettingOption(
    modifier: Modifier = Modifier,
    text: String,
    label: String? = null,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .applyIf(onClick != null, ifTrue = { clickable { onClick?.invoke() } })
            .fillMaxWidth()
            .applyIf(
                condition = label != null,
                ifTrue = { padding(16.dp) },
                ifFalse = { padding(16.dp, 26.dp) }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            label?.let {
                Text(
                    text = it,
                    style = PassTypography.body3RegularWeak,
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(condition = isLoading, ifTrue = { placeholder() }),
                text = text,
                style = ProtonTheme.typography.defaultWeak,
                color = PassTheme.colors.textNorm
            )
        }
        if (onClick != null) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_tiny_right),
                contentDescription = stringResource(R.string.setting_option_icon_content_description),
                tint = PassTheme.colors.textHint
            )
        }
    }
}

@Preview
@Composable
fun SettingOptionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SettingOption(text = "Match system", label = "Theme", onClick = {})
        }
    }
}
