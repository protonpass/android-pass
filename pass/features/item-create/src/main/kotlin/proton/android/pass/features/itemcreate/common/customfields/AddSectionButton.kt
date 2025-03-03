/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.itemcreate.common.customfields

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R

@Composable
fun AddSectionButton(
    modifier: Modifier = Modifier,
    passItemColors: PassItemColors,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button.Circular(
        modifier = modifier.fillMaxWidth(),
        color = PassTheme.colors.backgroundNorm,
        borderStroke = BorderStroke(1.dp, PassTheme.colors.inputBorderNorm),
        contentPadding = PaddingValues(Spacing.mediumSmall),
        enabled = isEnabled,
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            val adjustedTextColor = if (isEnabled) {
                passItemColors.majorSecondary
            } else {
                passItemColors.majorSecondary.copy(alpha = 0.3f)
            }
            Icon.Default(
                modifier = Modifier.size(ProtonTheme.typography.body1Regular.fontSize.value.dp),
                id = R.drawable.ic_add_section,
                tint = adjustedTextColor
            )
            Text.Body1Regular(
                text = stringResource(R.string.add_section_button),
                color = adjustedTextColor
            )
        }
    }
}

@Preview
@Composable
fun AddSectionButtonPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AddSectionButton(
                isEnabled = true,
                passItemColors = passItemColors(ItemCategory.Custom),
                onClick = {}
            )
        }
    }
}
