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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.upsell.v1.R
import proton.android.pass.features.upsell.v2.models.UpsellPlanUiModel
import proton.android.pass.features.upsell.v2.models.elementsPlusPlan
import proton.android.pass.features.upsell.v2.models.elementsUnlimitedPlan
import proton.android.pass.features.upsell.v2.presentation.UpsellV2Section
import proton.android.pass.features.upsell.v2.presentation.mocks.mockAnnualPlans

internal const val WEIGHT_PASS_COL1 = 0.50f
internal const val WEIGHT_PASS_COL2 = 0.25f
internal const val WEIGHT_PASS_COL3 = 0.25f

internal const val WEIGHT_UNLIMITED_COL1 = 0.42f
internal const val WEIGHT_UNLIMITED_COL2 = 0.25f
internal const val WEIGHT_UNLIMITED_COL3 = 0.38f

@Composable
fun UpsellAnnualPlan(
    modifier: Modifier = Modifier,
    plans: List<UpsellPlanUiModel>,
    onPaymentCallback: (ProtonPaymentEvent) -> Unit
) {
    val (selectedPlanIndex, onUpdateSelectedPlanIndex) = remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = Spacing.mediumLarge)
                .statusBarsPadding()
                .padding(top = 140.dp)
        ) {
            Text(
                text = stringResource(id = R.string.upsell_plan_title),
                style = ProtonTheme.typography.hero.copy(
                    color = ProtonTheme.colors.textNorm
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (plans.isNotEmpty()) {
                ProtonSelector(
                    items = plans.map { it.selector },
                    selectedIndex = selectedPlanIndex,
                    onItemSelected = onUpdateSelectedPlanIndex
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = selectedPlanIndex == 1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.upsell_plan_unlimited_title),
                            style = ProtonTheme.typography.body2Regular.copy(
                                color = ProtonTheme.colors.textNorm
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 48.dp),
                            painter = painterResource(
                                id = if (LocalDark.current)
                                    R.drawable.upsell_plan_everything
                                else
                                    R.drawable.upsell_plan_everything_light
                            ),
                            contentDescription = ""
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (plans.isNotEmpty()) {
                    AnimatedContent(
                        targetState = selectedPlanIndex,
                        transitionSpec = {
                            fadeIn().togetherWith(fadeOut())
                        }
                    ) { s ->
                        if (s == 0) {
                            UpsellV2Section(
                                modifier = Modifier.fillMaxWidth(),
                                leftColumnText = "Free",
                                rightColumnText = "Plus",
                                items = elementsPlusPlan,
                                weightCol1 = WEIGHT_PASS_COL1,
                                weightCol2 = WEIGHT_PASS_COL2,
                                weightCol3 = WEIGHT_PASS_COL3
                            )
                        } else if (s == 1) {
                            UpsellV2Section(
                                modifier = Modifier.fillMaxWidth(),
                                leftColumnText = "Plus",
                                leftColumnBackTextColor = if (LocalDark.current)
                                    PassTheme.colors.backgroundMedium
                                else
                                    PassPalette.upsellLightBackgroundColor,
                                rightColumnText = "Unlimited",
                                rightColumnTextColor = PassTheme.colors.textInvert,
                                rightColumnBackTextColor = Color.White,
                                items = elementsUnlimitedPlan,
                                weightCol1 = WEIGHT_UNLIMITED_COL1,
                                weightCol2 = WEIGHT_UNLIMITED_COL2,
                                weightCol3 = WEIGHT_UNLIMITED_COL3
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.mediumLarge))

            // for scrolling
            Spacer(modifier = Modifier.height(BOTTOM_AREA_MIN_HEIGHT))
        }

        plans.getOrNull(selectedPlanIndex)?.let {
            BottomArea(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter),
                bottomText = it.bottomAnnualPrice,
                paymentButtonUiState = it.paymentButtonUiState,
                onPaymentCallback = onPaymentCallback
            )
        }
    }
}

@Preview
@Composable
fun UpsellAnnualPlanPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellAnnualPlan(
                plans = mockAnnualPlans,
                onPaymentCallback = {}
            )
        }
    }
}
