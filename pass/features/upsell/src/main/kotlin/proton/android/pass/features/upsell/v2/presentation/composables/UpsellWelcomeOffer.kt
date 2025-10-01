/*
 * Copyright (c) 2025 Proton AG
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.upsell.v1.R
import proton.android.pass.features.upsell.v2.models.StepToDisplay
import proton.android.pass.features.upsell.v2.models.UpsellPlanUiModel
import proton.android.pass.features.upsell.v2.presentation.composables.welcomeOffer.GradientText
import proton.android.pass.features.upsell.v2.presentation.composables.welcomeOffer.GradientTextLimited
import proton.android.pass.features.upsell.v2.presentation.composables.welcomeOffer.WhatsIncluded
import proton.android.pass.features.upsell.v2.presentation.mocks.mockWelcomeMonthlyPlan
import proton.android.pass.composecomponents.impl.R as ComposeR

@Composable
fun UpsellWelcomeOffer(
    modifier: Modifier = Modifier,
    stepToDisplay: StepToDisplay,
    plan: UpsellPlanUiModel,
    onPaymentCallback: (ProtonPaymentEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PassTheme.colors.backgroundWelcomeBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = Spacing.mediumLarge)
                .statusBarsPadding()
                .padding(top = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(top = Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                val image = ImageBitmap.imageResource(R.drawable.welcome_background)

                Image.Default(
                    modifier = Modifier
                        .size(120.dp)
                        .drawBehind {
                            val dstWidth = size.width
                            val dstHeight = size.height

                            val srcWidth = image.width.toFloat()
                            val srcHeight = image.height.toFloat()

                            drawImage(
                                image = image,
                                topLeft = Offset(
                                    x = (dstWidth - srcWidth) / 2f,
                                    y = (dstHeight - srcHeight) / 2f
                                )
                            )
                        },
                    id = ComposeR.drawable.logo_mark
                )

                Image.Default(
                    id = R.drawable.upsell_plus,
                    modifier = Modifier
                        .size(size = 50.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = Spacing.small, y = Spacing.small)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            GradientTextLimited(
                text = stringResource(R.string.upsell_limited_time_offer).uppercase()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text.Headline(
                text = stringResource(
                    when (stepToDisplay) {
                        StepToDisplay.WelcomeOfferMonthly -> R.string.upsell_try_pass_plus
                        StepToDisplay.WelcomeOfferYearly -> R.string.upsell_pass_plus_welcome_offer
                        else -> {
                            throw IllegalArgumentException("Unknown step : $stepToDisplay")
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            // middle price
            GradientText(
                text = if (stepToDisplay == StepToDisplay.WelcomeOfferMonthly) {
                    plan.selector.pricePerMonth
                } else {
                    plan.pricePerYear
                },
                textSize = 54f
            )

            if (stepToDisplay == StepToDisplay.WelcomeOfferMonthly) {
                val color = ProtonTheme.colors.textWeak
                Text(
                    modifier = Modifier.drawWithContent {
                        drawContent()
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height / 2
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokeWidth
                        )
                    },
                    text = plan.defaultPricePerMonth.orEmpty(),
                    style = ProtonTheme.typography.body1Bold.copy(
                        color = color
                    )
                )
            } else {
                GradientText(
                    text = stringResource(R.string.upsell_pass_plus_first_year),
                    textSize = 15f
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text.Body2Regular(
                text = if (stepToDisplay == StepToDisplay.WelcomeOfferMonthly) {
                    stringResource(R.string.upsell_get_the_best_of_pass)
                } else {
                    stringResource(
                        R.string.price_per_month_type2,
                        plan.selector.pricePerMonth
                    )
                }
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .height(1.dp),
                color = PassTheme.colors.textWeak.copy(alpha = 0.1f)
            )

            WhatsIncluded(
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.mediumLarge))

            // for scrolling
            Spacer(modifier = Modifier.height(BOTTOM_AREA_MIN_HEIGHT))
        }

        BottomArea(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter),
            bottomText = plan.bottomAnnualPrice,
            paymentButtonUiState = plan.paymentButtonUiState,
            onPaymentCallback = onPaymentCallback,
            backgroundColor = if (LocalDark.current) {
                Color(color = 0xFF472A6A)
            } else {
                PassTheme.colors.backgroundStrong
            }
        )
    }
}

@Preview
@Composable
fun UpsellWelcomeOfferPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellWelcomeOffer(
                stepToDisplay = StepToDisplay.WelcomeOfferMonthly,
                plan = mockWelcomeMonthlyPlan[0],
                onPaymentCallback = {}
            )
        }
    }
}

