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

package proton.android.pass.features.secure.links.create.ui.rows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.create.ui.SecureLinksCreateUiEvent
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinkCreateMaxViewsRow(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit,
    isConfigurationAllowed: Boolean,
    isMaxViewsEnabled: Boolean,
    isMaxViewsDecreaseEnabled: Boolean,
    maxViewsAllowed: Int
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium)
    ) {
        SecureLinkCreateMaxViewsRowHeader(
            modifier = Modifier.fillMaxWidth(),
            isConfigurationAllowed = isConfigurationAllowed,
            isMaxViewsEnabled = isMaxViewsEnabled,
            onUiEvent = onUiEvent
        )

        SecureLinkCreateMaxViewsRowCounter(
            modifier = Modifier
                .padding(start = Spacing.large)
                .offset(y = -Spacing.small),
            isConfigurationAllowed = isConfigurationAllowed,
            isMaxViewsEnabled = isMaxViewsEnabled,
            isMaxViewsDecreaseEnabled = isMaxViewsDecreaseEnabled,
            maxViewsAllowed = maxViewsAllowed,
            onUiEvent = onUiEvent
        )
    }
}

@Composable
private fun SecureLinkCreateMaxViewsRowHeader(
    modifier: Modifier = Modifier,
    isConfigurationAllowed: Boolean,
    isMaxViewsEnabled: Boolean,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_eye),
            contentDescription = null,
            tint = PassTheme.colors.textNorm
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f, fill = false),
            text = stringResource(id = R.string.secure_links_create_row_max_views_title),
            style = ProtonTheme.typography.body1Regular
        )

        Switch(
            enabled = isConfigurationAllowed,
            checked = isMaxViewsEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.interactionNormMajor1
            ),
            onCheckedChange = { newIsChecked ->
                if (newIsChecked) {
                    SecureLinksCreateUiEvent.OnEnableMaxViewsClicked
                } else {
                    SecureLinksCreateUiEvent.OnDisableMaxViewsClicked
                }.also(onUiEvent)
            }
        )
    }
}

@Composable
private fun ColumnScope.SecureLinkCreateMaxViewsRowCounter(
    modifier: Modifier = Modifier,
    isConfigurationAllowed: Boolean,
    isMaxViewsEnabled: Boolean,
    isMaxViewsDecreaseEnabled: Boolean,
    maxViewsAllowed: Int,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit
) {
    AnimatedVisibility(visible = isMaxViewsEnabled) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Text(
                text = stringResource(
                    id = R.string.secure_links_create_row_max_views_subtitle,
                    maxViewsAllowed
                ),
                style = ProtonTheme.typography.body2Regular,
                color = PassTheme.colors.textWeak
            )

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color = PassTheme.colors.interactionNormMinor1),
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = isConfigurationAllowed && isMaxViewsDecreaseEnabled,
                    onClick = { onUiEvent(SecureLinksCreateUiEvent.OnDecreaseMaxViewsClicked) }
                ) {
                    Icon(
                        modifier = Modifier.size(size = Spacing.medium),
                        painter = painterResource(CoreR.drawable.ic_proton_minus),
                        contentDescription = null
                    )
                }

                Box(modifier = Modifier.defaultMinSize(minWidth = 20.dp)) {
                    Text(
                        modifier = Modifier.align(alignment = Alignment.Center),
                        text = maxViewsAllowed.toString(),
                        style = ProtonTheme.typography.body1Bold
                    )
                }

                IconButton(
                    enabled = isConfigurationAllowed,
                    onClick = { onUiEvent(SecureLinksCreateUiEvent.OnIncreaseMaxViewsClicked) }
                ) {
                    Icon(
                        modifier = Modifier.size(size = Spacing.medium),
                        painter = painterResource(CoreR.drawable.ic_proton_plus),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@[Preview Composable]
internal fun SecureLinkCreateMaxViewsRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isEnabled) = input

    PassTheme(isDark = isDark) {
        Surface {
            SecureLinkCreateMaxViewsRow(
                isConfigurationAllowed = true,
                isMaxViewsEnabled = isEnabled,
                isMaxViewsDecreaseEnabled = false,
                maxViewsAllowed = 1,
                onUiEvent = {}
            )
        }
    }
}
