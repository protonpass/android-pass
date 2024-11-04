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

package proton.android.pass.features.inappmessages.banner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTARoute
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageMode
import me.proton.core.presentation.R as CoreR

@Composable
fun InAppMessageBanner(
    modifier: Modifier = Modifier,
    inAppMessage: InAppMessage,
    onCTAClick: (InAppMessageCTARoute) -> Unit,
    onDismiss: (InAppMessageId) -> Unit
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = Spacing.medium + Spacing.extraSmall,
                    vertical = Spacing.medium
                )
                .roundedContainer(
                    backgroundColor = PassTheme.colors.backgroundWeak,
                    borderColor = PassTheme.colors.inputBorderNorm
                )
                .applyIf(
                    condition = inAppMessage.ctaRoute is Some,
                    ifTrue = {
                        when (val route = inAppMessage.ctaRoute) {
                            None -> Modifier
                            is Some -> clickable { onCTAClick(route.value) }
                        }
                    }
                )
                .padding(Spacing.mediumSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (val url = inAppMessage.imageUrl) {
                None -> {}
                is Some -> AsyncImage(
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(1f, true),
                    contentScale = ContentScale.Fit,
                    model = url.value,
                    placeholder = if (LocalInspectionMode.current) {
                        ColorPainter(Color.Red)
                    } else {
                        null
                    },
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text.CaptionMedium(inAppMessage.title)
                when (val ctaText = inAppMessage.ctaText) {
                    None -> {}
                    is Some -> Text.CaptionRegular(ctaText.value)
                }
            }
            Icon.Default(
                id = CoreR.drawable.ic_proton_chevron_right,
                tint = PassTheme.colors.textWeak
            )
        }
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = { onDismiss(inAppMessage.id) }
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 2.dp,
                        color = PassTheme.colors.backgroundNorm,
                        shape = CircleShape
                    )
                    .padding(2.dp)
                    .background(
                        color = PassTheme.colors.backgroundMedium,
                        shape = CircleShape
                    )
                    .padding(Spacing.extraSmall),
                painter = painterResource(CoreR.drawable.ic_proton_cross_small),
                tint = ProtonTheme.colors.iconNorm,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
fun InAppBannerPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InAppMessageBanner(
                inAppMessage = InAppMessage(
                    id = InAppMessageId("1"),
                    mode = InAppMessageMode.Banner,
                    title = "Title",
                    message = Some("Message"),
                    imageUrl = Some(""),
                    ctaRoute = Some(InAppMessageCTARoute("CTA")),
                    ctaText = None
                ),
                onCTAClick = {},
                onDismiss = {}
            )
        }
    }
}
