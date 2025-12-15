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

package proton.android.pass.features.upsell.v2.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.features.upsell.v1.R
import proton.android.pass.features.upsell.v2.models.StepToDisplay
import proton.android.pass.features.upsell.v2.models.UpsellV2UiState
import proton.android.pass.features.upsell.v2.presentation.composables.UpsellAnnualPlan
import proton.android.pass.features.upsell.v2.presentation.composables.UpsellWelcomeOffer
import proton.android.pass.features.upsell.v2.presentation.mocks.mockAnnualPlans
import proton.android.pass.features.upsell.v2.presentation.mocks.mockWelcomeMonthlyPlan
import proton.android.pass.features.upsell.v2.presentation.mocks.mockWelcomeYearlyPlan
import proton.android.pass.composecomponents.impl.R as ComposeR


@Composable
fun UpsellV2Content(
    modifier: Modifier = Modifier,
    uiState: UpsellV2UiState,
    onPaymentCallback: (ProtonPaymentEvent) -> Unit,
    onSkipButtonClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = PassTheme.colors.backgroundBrush)
    ) {
        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.AnnualPlans,
            enter = slideInVertically { -it },
            exit = slideOutVertically { it }
        ) {
            Image.Default(
                id = ComposeR.drawable.logo_planv2,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
            )
        }

        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.AnnualPlans &&
                uiState.plans.size == 2,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            UpsellAnnualPlan(
                plans = uiState.plans,
                onPaymentCallback = onPaymentCallback
            )
        }

        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.WelcomeOfferMonthly &&
                uiState.plans.size == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            UpsellWelcomeOffer(
                stepToDisplay = uiState.stepToDisplay,
                plan = uiState.plans[0],
                onPaymentCallback = onPaymentCallback
            )
        }

        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.WelcomeOfferYearly &&
                uiState.plans.size == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            UpsellWelcomeOffer(
                stepToDisplay = uiState.stepToDisplay,
                plan = uiState.plans[0],
                onPaymentCallback = onPaymentCallback
            )
        }

        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.NoPlans,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.upsell_no_plan),
                    style = ProtonTheme.typography.body2Regular.copy(
                        color = ProtonTheme.colors.textNorm
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.stepToDisplay == StepToDisplay.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ProtonTextButton(
            modifier = Modifier
                .padding(Spacing.mediumLarge, Spacing.none)
                .align(alignment = Alignment.TopEnd)
                .statusBarsPadding()
                .height(48.dp),
            onClick = onSkipButtonClick
        ) {
            Text(
                text = stringResource(R.string.upsell_skip),
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = ProtonTheme.colors.textNorm,
                style = ProtonTheme.typography.body1Regular
            )
        }

        AnimatedVisibility(
            visible = uiState.displayLoaderDuringPurchase,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.7f))
                    .clickable( // do not allow click anywhere during this special final loading
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview
@Composable
fun UpsellPlanContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        UpsellV2Content(
            onSkipButtonClick = {},
            uiState = UpsellV2UiState(
                stepToDisplay = StepToDisplay.AnnualPlans,
                plans = mockAnnualPlans
            ),
            onPaymentCallback = { _ -> }
        )
    }
}

@Preview
@Composable
fun UpsellPlanContentWelcomeMonthlyPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        UpsellV2Content(
            onSkipButtonClick = {},
            uiState = UpsellV2UiState(
                stepToDisplay = StepToDisplay.WelcomeOfferMonthly,
                plans = mockWelcomeMonthlyPlan
            ),
            onPaymentCallback = { _ -> }
        )
    }
}

@Preview
@Composable
fun UpsellPlanContentWelcomeYearlyPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        UpsellV2Content(
            onSkipButtonClick = {},
            uiState = UpsellV2UiState(
                stepToDisplay = StepToDisplay.WelcomeOfferYearly,
                plans = mockWelcomeYearlyPlan
            ),
            onPaymentCallback = { _ -> }
        )
    }
}

@Preview
@Composable
fun UpsellPlanContentLoadingPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        UpsellV2Content(
            onSkipButtonClick = {},
            uiState = UpsellV2UiState(
                stepToDisplay = StepToDisplay.Loading,
                plans = persistentListOf()
            ),
            onPaymentCallback = { _ -> }
        )
    }
}

@Preview
@Composable
fun UpsellPlanContentEmptyPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        UpsellV2Content(
            onSkipButtonClick = {},
            uiState = UpsellV2UiState(
                stepToDisplay = StepToDisplay.NoPlans,
                plans = persistentListOf()
            ),
            onPaymentCallback = { _ -> }
        )
    }
}
