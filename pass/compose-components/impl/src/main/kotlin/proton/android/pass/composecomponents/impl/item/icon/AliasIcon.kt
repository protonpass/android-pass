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

package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import me.proton.core.presentation.R as CoreR

@Composable
fun AliasIcon(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeAlias: Boolean = true,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    backgroundColor: Color = when {
        enabled && activeAlias -> PassTheme.colors.aliasInteractionNormMinor1
        !activeAlias -> PassTheme.colors.backgroundNorm
        else -> PassTheme.colors.aliasInteractionNormMinor2
    },
    foregroundColor: Color = when {
        enabled && activeAlias -> PassTheme.colors.aliasInteractionNormMajor2
        enabled && !activeAlias -> PassTheme.colors.aliasInteractionNormMajor2
        !enabled && !activeAlias -> PassTheme.colors.aliasInteractionNormMinor1
        else -> PassTheme.colors.aliasInteractionNormMinor1
    },
    borderColor: Color? = when {
        !activeAlias && enabled -> PassTheme.colors.aliasInteractionNormMinor1
        !activeAlias && !enabled -> PassTheme.colors.aliasInteractionNormMinor2
        else -> null
    }
) {
    val iconResourceId = remember(activeAlias) {
        if (activeAlias) {
            CoreR.drawable.ic_proton_alias
        } else {
            R.drawable.ic_alias_slash
        }
    }

    BoxedIcon(
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        size = size,
        shape = shape
    ) {
        Icon(
            modifier = Modifier.padding(Spacing.extraSmall),
            painter = painterResource(id = iconResourceId),
            contentDescription = null,
            tint = foregroundColor
        )
    }
}

internal class AliasIconPreviewProvider : PreviewParameterProvider<AliasIconParams> {
    override val values: Sequence<AliasIconParams> = sequence {
        for (isEnabled in listOf(true, false)) {
            for (isActive in listOf(true, false)) {
                yield(AliasIconParams(isEnabled, isActive))
            }
        }
    }
}

internal data class AliasIconParams(
    val enabled: Boolean,
    val active: Boolean
)

internal class ThemeAndAliasIconProvider : ThemePairPreviewProvider<AliasIconParams>(
    AliasIconPreviewProvider()
)

@Preview
@Composable
internal fun AliasIconPreview(
    @PreviewParameter(ThemeAndAliasIconProvider::class) input: Pair<Boolean, AliasIconParams>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasIcon(
                enabled = input.second.enabled,
                activeAlias = input.second.active
            )
        }
    }
}
