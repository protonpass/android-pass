/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ProfileSecureLinksSection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    shouldShowPlusIcon: Boolean,
    secureLinksCount: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .roundedContainerNorm()
            .clickable { onClick() }
            .padding(horizontal = Spacing.medium)
            .applyIf(
                condition = shouldShowPlusIcon,
                ifTrue = { padding(vertical = 26.dp) },
                ifFalse = { padding(vertical = 18.dp) }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.profile_option_secure_links),
            style = ProtonTheme.typography.defaultWeak,
            color = PassTheme.colors.textNorm
        )

        if (shouldShowPlusIcon) {
            PassPlusIcon()
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                Circle(
                    backgroundColor = PassTheme.colors.backgroundMedium
                ) {
                    Text(
                        text = secureLinksCount.toString(),
                        color = PassTheme.colors.textNorm,
                        style = ProtonTheme.typography.body1Medium
                    )
                }

                Icon(
                    painter = painterResource(CompR.drawable.ic_chevron_tiny_right),
                    contentDescription = null,
                    tint = PassTheme.colors.textHint
                )
            }
        }
    }
}

@[Preview Composable]
internal fun ProfileSecureLinksSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, showPlusIcon) = input

    PassTheme(isDark = isDark) {
        Surface {
            ProfileSecureLinksSection(
                onClick = {},
                shouldShowPlusIcon = showPlusIcon,
                secureLinksCount = 4
            )
        }
    }
}
