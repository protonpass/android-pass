/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.upsell.v2.presentation.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonpresentation.api.plan.PaymentButtonUiState
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleButton

internal val BOTTOM_AREA_MIN_HEIGHT = 150.dp

@Composable
internal fun BottomArea(
    modifier: Modifier = Modifier,
    paymentButtonUiState: PaymentButtonUiState,
    bottomText: String,
    backgroundColor: Color = if (LocalDark.current) {
        PassTheme.colors.inputBackgroundStrong
    } else {
        PassPalette.upsellLightBackgroundColor
    },
    onPurchaseClick: () -> Unit
) {
    var bottomAreaVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { bottomAreaVisible = true }

    AnimatedVisibility(
        modifier = modifier,
        visible = bottomAreaVisible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = BOTTOM_AREA_MIN_HEIGHT)
                .background(color = backgroundColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            CircleButton(
                modifier = Modifier
                    .padding(Spacing.mediumLarge, Spacing.none)
                    .fillMaxWidth()
                    .height(52.dp),
                color = PassTheme.colors.signalNorm,
                onClick = onPurchaseClick,
                content = {
                    Text(
                        text = paymentButtonUiState.defaultButtonText
                            ?: paymentButtonUiState.formattedPrice,
                        style = ProtonTheme.typography.body2Regular.copy(
                            color = PassTheme.colors.interactionNormMinor2
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedContent(
                targetState = bottomText,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { t ->
                Text(
                    text = t,
                    style = ProtonTheme.typography.body2Regular.copy(
                        color = ProtonTheme.colors.textNorm
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Spacer(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }
}

@Preview
@Composable
fun BottomAreaPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            BottomArea(
                paymentButtonUiState = PaymentButtonUiState(
                    productId = "pass_plus_12",
                    offerToken = "preview-offer-token",
                    formattedPrice = "$9.99"
                ),
                bottomText = "The bottom text displayed",
                onPurchaseClick = {}
            )
        }
    }
}
