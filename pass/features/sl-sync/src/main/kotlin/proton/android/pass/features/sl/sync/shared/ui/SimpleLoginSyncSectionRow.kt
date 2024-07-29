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

package proton.android.pass.features.sl.sync.shared.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncSectionRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    label: String? = null,
    description: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        label?.let { labelText ->
            SimpleLoginSyncLabelText(
                text = labelText
            )
        }

        Row(
            modifier = Modifier
                .roundedContainerNorm()
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            leadingIcon?.invoke()

            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
            ) {
                SectionTitle(
                    text = title
                )

                SectionSubtitle(
                    text = subtitle.asAnnotatedString()
                )
            }

            Icon(
                painter = painterResource(id = CompR.drawable.ic_chevron_tiny_right),
                contentDescription = null,
                tint = PassTheme.colors.textWeak
            )
        }

        description?.let { descriptionText ->
            SimpleLoginSyncDescriptionText(
                text = descriptionText
            )
        }
    }
}

@[Preview Composable]
internal fun SimpleLoginSyncSectionRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncSectionRow(
                title = "SL sync title",
                subtitle = "SL sync subtitle",
                label = "SL sync label",
                onClick = {}
            )
        }
    }
}

@[Preview Composable]
internal fun SimpleLoginSyncSectionRowVaultPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SimpleLoginSyncSectionRow(
                title = "SL sync title",
                subtitle = "SL sync subtitle",
                description = "SL sync description",
                leadingIcon = {
                    VaultIcon(
                        backgroundColor = ShareColor.Color1.toColor(isBackground = true),
                        icon = ShareIcon.Icon1.toResource(),
                        iconColor = ShareColor.Color1.toColor()
                    )
                },
                onClick = {}
            )
        }
    }
}
