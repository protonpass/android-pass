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

package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ExtraPassword(
    modifier: Modifier = Modifier,
    isExtraPasswordEnabled: Boolean,
    onEvent: (AccountContentEvent) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        if (isExtraPasswordEnabled) {
            ColorSettingOption(
                modifier = Modifier.roundedContainerNorm(),
                text = stringResource(R.string.account_settings_list_item_extra_password_header),
                textColor = ProtonTheme.colors.textNorm,
                iconBgColor = Color.Transparent,
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(CompR.drawable.ic_three_dots_vertical_24),
                        contentDescription = stringResource(id = CompR.string.action_content_description_menu),
                        tint = ProtonTheme.colors.iconWeak
                    )
                },
                subtitle = {
                    Text(
                        text = "Active",
                        style = PassTheme.typography.body3Norm()
                            .copy(color = PassTheme.colors.cardInteractionNormMajor1)
                    )
                },
                onClick = { onEvent(AccountContentEvent.RemoveExtraPassword) }
            )
        } else {
            ColorSettingOption(
                modifier = Modifier.roundedContainerNorm(),
                innerModifier = Modifier.padding(vertical = Spacing.medium),
                text = stringResource(R.string.account_settings_list_item_extra_password_header),
                textColor = PassTheme.colors.interactionNormMajor2,
                iconBgColor = PassTheme.colors.interactionNormMinor1,
                onClick = { onEvent(AccountContentEvent.SetExtraPassword) }
            )
        }

        Text(
            text = stringResource(R.string.account_settings_list_item_extra_password_description),
            style = PassTheme.typography.body3Weak()
        )
    }
}

@Preview
@Composable
fun ExtraPasswordPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            ExtraPassword(isExtraPasswordEnabled = input.second, onEvent = {})
        }
    }
}
