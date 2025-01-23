/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.banner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.banner.FeatureDiscoveryBanner
import proton.android.pass.composecomponents.impl.banner.FeatureDiscoveryText
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.R as CoreR

@Composable
fun AliasMailboxBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    FeatureDiscoveryBanner(
        modifier = modifier,
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeatureDiscoveryText(
                    modifier = Modifier
                        .padding(Spacing.medium)
                        .weight(1f, fill = false),
                    title = stringResource(R.string.banner_mailbox_title),
                    body = stringResource(R.string.banner_mailbox_subtitle)
                )
                Button.Circular(
                    modifier = Modifier
                        .padding(end = Spacing.medium)
                        .padding(vertical = Spacing.large),
                    color = Color.White,
                    onClick = onClick
                ) {
                    Text.CaptionMedium(
                        text = stringResource(R.string.banner_mailbox_button),
                        color = PassPalette.EerieBlack
                    )
                }
            }
        },
        closeIcon = {
            Icon.Default(
                id = CoreR.drawable.ic_proton_cross_small, tint = PassPalette.MistyGray
            )
        },
        onClose = onClose
    )
}

@Preview
@Composable
fun AliasMailboxBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AliasMailboxBanner(
                onClick = {},
                onClose = {}
            )
        }
    }
}
