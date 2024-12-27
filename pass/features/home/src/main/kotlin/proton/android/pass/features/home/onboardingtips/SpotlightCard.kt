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

package proton.android.pass.features.home.onboardingtips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlightNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.home.R

@Composable
fun SpotlightCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    title: String,
    body: String,
    caption: String? = null,
    titleColor: Color = PassTheme.colors.textInvert,
    subtitleColor: Color = titleColor,
    buttonColor: Color = titleColor,
    crossColor: Color = titleColor,
    buttonText: String?,
    image: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)?
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 10.dp
    ) {
        Box(
            modifier = Modifier.background(backgroundColor)
        ) {
            Row(
                modifier = Modifier.padding(Spacing.medium, Spacing.large),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Text(
                        text = title,
                        style = ProtonTheme.typography.defaultHighlightNorm,
                        color = titleColor
                    )
                    Text(
                        text = body,
                        style = PassTheme.typography.body3Norm(),
                        color = subtitleColor
                    )

                    caption?.let {
                        Spacer(modifier = Modifier.height(Spacing.extraSmall))
                        Text.CaptionMedium(text = it, color = titleColor)
                    }
                    if (buttonText != null) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = buttonText,
                            style = ProtonTheme.typography.defaultHighlightNorm,
                            color = buttonColor
                        )
                    }
                }
                image?.invoke()
            }

            if (onDismiss != null) {
                SmallCrossIconButton(
                    modifier = Modifier.align(Alignment.TopEnd),
                    tint = crossColor,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Preview
@Composable
fun SpotlightCardPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SpotlightCard(
                backgroundColor = PassTheme.colors.loginInteractionNorm,
                title = "A sample card",
                body = "A sample body with a very long text that can go multiline",
                buttonText = "Click me",
                image = {
                    Image(
                        modifier = Modifier.size(60.dp),
                        alignment = Alignment.CenterEnd,
                        painter = painterResource(id = R.drawable.spotlight_illustration),
                        contentDescription = null
                    )
                },
                onClick = {},
                onDismiss = {}
            )
        }
    }
}
