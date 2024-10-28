/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun GeneratePasswordSelectorRow(
    modifier: Modifier = Modifier,
    title: String,
    selectedValue: String,
    onClick: () -> Unit,
    isSelectable: Boolean = true,
    iconContentDescription: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text.Body2Regular(
            text = title,
            color = PassTheme.colors.textNorm
        )

        Row(
            modifier = Modifier
                .padding(vertical = Spacing.small)
                .clip(RoundedCornerShape(size = Radius.small))
                .applyIf(
                    condition = isSelectable,
                    ifTrue = {
                        background(PassTheme.colors.loginInteractionNormMajor1)
                            .clickable(onClick = onClick)
                            .padding(
                                start = Spacing.small,
                                top = Spacing.extraSmall,
                                bottom = Spacing.extraSmall,
                                end = Spacing.extraSmall
                            )
                    },
                    ifFalse = {
                        background(PassTheme.colors.loginInteractionNormMinor1)
                            .padding(all = Spacing.small)
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text.Body3Medium(
                text = selectedValue,
                color = if (isSelectable) PassTheme.colors.textInvert else PassTheme.colors.textNorm
            )

            if (isSelectable) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_chevron_tiny_down),
                    contentDescription = iconContentDescription,
                    tint = PassTheme.colors.textInvert
                )
            }
        }
    }
}

@[Preview Composable]
internal fun GeneratePasswordSelectorRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isSelectable) = input

    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordSelectorRow(
                title = "Title",
                selectedValue = "Selected option value",
                isSelectable = isSelectable,
                iconContentDescription = null,
                onClick = {}
            )
        }
    }
}
