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

package proton.android.pass.features.inappmessages.promo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import coil.compose.AsyncImage
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.defaultTint
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageCTA
import proton.android.pass.domain.inappmessages.InAppMessageCTAType
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageMode
import proton.android.pass.domain.inappmessages.InAppMessagePromoContents
import proton.android.pass.domain.inappmessages.InAppMessagePromoThemedContents
import proton.android.pass.domain.inappmessages.InAppMessageRange
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import me.proton.core.presentation.R as CoreR

@Composable
fun InAppMessagePromoContent(
    modifier: Modifier = Modifier,
    inAppMessage: InAppMessage,
    onInternalCTAClick: (String) -> Unit,
    onExternalCTAClick: (String) -> Unit,
    onMinimize: () -> Unit,
    onDontShowAgain: () -> Unit
) {
    val promo = inAppMessage.promoContents.value() ?: return
    val isDarkTheme = isSystemInDarkTheme()
    val themeContents = remember(isDarkTheme) {
        if (isDarkTheme) {
            promo.darkThemeContents
        } else {
            promo.lightThemeContents
        }
    }
    val textColor = remember(themeContents.closePromoTextColor) {
        runCatching { Color(themeContents.closePromoTextColor.toInt()).copy(alpha = 1f) }
    }.fold({ it }, { defaultTint() })

    Box(modifier = modifier) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            model = themeContents.backgroundImageUrl,
            placeholder = if (LocalInspectionMode.current) {
                ColorPainter(Color.Red)
            } else {
                null
            },
            contentDescription = null
        )
        Scaffold(
            modifier = Modifier.systemBarsPadding(),
            backgroundColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onMinimize) {
                        Icon.Default(
                            id = CoreR.drawable.ic_proton_cross_circle_filled,
                            tint = textColor
                        )
                    }
                }
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .applyIf(inAppMessage.cta is Some, ifTrue = {
                            clickable {
                                val cta = inAppMessage.cta.value()
                                when (cta?.type) {
                                    InAppMessageCTAType.Internal -> onInternalCTAClick(cta.route)
                                    InAppMessageCTAType.External -> onExternalCTAClick(cta.route)
                                    InAppMessageCTAType.Unknown -> {}
                                    else -> {}
                                }
                            }
                        }),
                    contentScale = ContentScale.Fit,
                    model = themeContents.contentImageUrl,
                    placeholder = if (LocalInspectionMode.current) {
                        ColorPainter(Color.Blue)
                    } else {
                        null
                    },
                    contentDescription = null
                )

                TransparentTextButton(
                    text = promo.closePromoText,
                    color = textColor,
                    onClick = onDontShowAgain
                )
            }
        }
    }
}

@Preview
@Composable
fun InAppMessagePromoContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InAppMessagePromoContent(
                inAppMessage = InAppMessage(
                    id = InAppMessageId("q"),
                    key = InAppMessageKey(""),
                    userId = UserId(""),
                    title = "",
                    message = None,
                    imageUrl = None,
                    mode = InAppMessageMode.Promo,
                    cta = InAppMessageCTA(
                        text = "Upgrade",
                        route = "pass://upgrade",
                        type = InAppMessageCTAType.Internal
                    ).some(),
                    state = InAppMessageStatus.Unread,
                    priority = 1,
                    range = InAppMessageRange(
                        start = Instant.DISTANT_PAST,
                        end = Some(Instant.DISTANT_FUTURE)
                    ),
                    promoContents = InAppMessagePromoContents(
                        startMinimised = false,
                        closePromoText = "Don't show this offer again",
                        lightThemeContents = InAppMessagePromoThemedContents(
                            backgroundImageUrl = "",
                            contentImageUrl = "",
                            closePromoTextColor = ""
                        ),
                        darkThemeContents = InAppMessagePromoThemedContents(
                            backgroundImageUrl = "",
                            contentImageUrl = "",
                            closePromoTextColor = ""
                        )
                    ).some()
                ),
                onInternalCTAClick = {},
                onExternalCTAClick = {},
                onMinimize = {},
                onDontShowAgain = {}
            )
        }
    }
}
