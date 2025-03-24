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

package proton.android.pass.features.security.center.breachdetail.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun RecommendedAction(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int,
    url: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = PassTheme.colors.inputBackgroundNorm,
                borderColor = PassTheme.colors.inputBackgroundNorm
            )
            .applyIf(
                condition = url != null,
                ifTrue = { clickable(onClick = onClick) }
            )
            .fillMaxWidth()
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = PassTheme.colors.interactionNormMajor2
        )

        Text.Body2Regular(
            modifier = Modifier.weight(1f),
            text = text
        )

        if (url != null) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
                contentDescription = null
            )
        }
    }
}

@[Preview Composable]
internal fun RecommendedActionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val url = if (input.second) "test.url" else null

    PassTheme(isDark = input.first) {
        Surface {
            RecommendedAction(
                text = "A recommended action",
                icon = R.drawable.ic_proton_key,
                url = url,
                onClick = {}
            )
        }
    }
}
