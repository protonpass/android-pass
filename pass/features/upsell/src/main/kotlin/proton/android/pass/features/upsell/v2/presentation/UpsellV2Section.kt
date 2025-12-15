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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.upsell.v1.R
import proton.android.pass.features.upsell.v2.models.PlanTypeUiState
import proton.android.pass.features.upsell.v2.models.UpsellItemsUiState
import proton.android.pass.features.upsell.v2.models.elementsPlusPlan
import proton.android.pass.features.upsell.v2.models.elementsUnlimitedPlan
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_PASS_COL1
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_PASS_COL2
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_PASS_COL3
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_UNLIMITED_COL1
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_UNLIMITED_COL2
import proton.android.pass.features.upsell.v2.presentation.composables.WEIGHT_UNLIMITED_COL3

@Composable
internal fun UpsellV2Section(
    modifier: Modifier = Modifier,
    leftColumnText: String,
    leftColumnBackTextColor: Color = Color.Transparent,
    leftColumnTextColor: Color = ProtonTheme.colors.textNorm,
    rightColumnText: String,
    rightColumnTextColor: Color = ProtonTheme.colors.textNorm,
    rightColumnBackTextColor: Color = PassTheme.colors.backgroundStrongest,
    rightColumnBackgroundColor: Color = if (LocalDark.current)
        PassTheme.colors.backgroundMedium
    else
        PassPalette.upsellLightBackgroundColor,
    items: List<UpsellItemsUiState>,
    weightCol1: Float = 2f,
    weightCol2: Float = 1f,
    weightCol3: Float = 1f
) {
    Column(
        modifier = modifier
    ) {
        // it's an empty row except for the last column which is a rounded top edge
        Row(modifier = Modifier.height(16.dp)) {
            Spacer(
                modifier = Modifier
                    .weight(weightCol1)
                    .fillMaxHeight()
            )
            Spacer(
                modifier = Modifier
                    .weight(weightCol2)
                    .fillMaxHeight()
            )
            Spacer(
                modifier = Modifier
                    .weight(weightCol3)
                    .fillMaxHeight()
                    .background(
                        color = rightColumnBackgroundColor,
                        shape = RoundedCornerShape(
                            CornerSize(24.dp),
                            CornerSize(24.dp),
                            CornerSize(0),
                            CornerSize(0)
                        )
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(weightCol1),
                text = stringResource(id = R.string.upsell_plan_what_included),
                style = ProtonTheme.typography.body1Bold.copy(
                    color = ProtonTheme.colors.textNorm
                )
            )

            Box(
                modifier = Modifier.weight(weightCol2),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            if (leftColumnBackTextColor != Color.Transparent) {
                                Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(leftColumnBackTextColor)
                                    .padding(horizontal = 12.dp)
                                    .padding(vertical = 4.dp)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // title
                    Text(
                        text = leftColumnText,
                        style = ProtonTheme.typography.body1Bold.copy(
                            color = leftColumnTextColor
                        ),
                        maxLines = 1
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(weightCol3)
                    .background(color = rightColumnBackgroundColor)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .then(
                            if (rightColumnBackTextColor != Color.Transparent) {
                                Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(rightColumnBackTextColor)
                                    .padding(horizontal = 12.dp)
                                    .padding(vertical = 4.dp)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // title
                    Text(
                        text = rightColumnText,
                        style = ProtonTheme.typography.body1Bold.copy(
                            color = rightColumnTextColor
                        ),
                        maxLines = 1
                    )
                }
            }
        }

        // all items
        items.forEachIndexed { index, it ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(weightCol1),
                    text = stringResource(id = it.title),
                    style = ProtonTheme.typography.body1Regular.copy(
                        color = ProtonTheme.colors.textNorm
                    ),
                    textAlign = TextAlign.Start
                )

                OnePlan(
                    modifier = Modifier
                        .weight(weightCol2),

                    plan = it.from,
                    textAlign = TextAlign.Center
                )

                OnePlan(
                    modifier = Modifier
                        .weight(weightCol3)
                        .background(
                            color = rightColumnBackgroundColor,
                            shape = RectangleShape
                        )
                        .padding(vertical = Spacing.extraSmall)
                        .then(
                            other = if (index == items.lastIndex) {
                                Modifier.padding(vertical = 8.dp)
                            } else {
                                Modifier.padding(vertical = 16.dp)
                            }
                        ),
                    plan = it.to,
                    textAlign = TextAlign.Center
                )
            }
        }
        // it's an empty row except for the last column which is a rounded bottom edge
        Row(modifier = Modifier.height(16.dp)) {
            Spacer(
                modifier = Modifier
                    .weight(weightCol1)
                    .fillMaxHeight()
            )
            Spacer(
                modifier = Modifier
                    .weight(weightCol2)
                    .fillMaxHeight()
            )
            Spacer(
                modifier = Modifier
                    .weight(weightCol3)
                    .fillMaxHeight()
                    .background(
                        color = rightColumnBackgroundColor,
                        shape = RoundedCornerShape(
                            CornerSize(0),
                            CornerSize(0),
                            CornerSize(24.dp),
                            CornerSize(24.dp)
                        )
                    )
            )
        }
    }
}


@Composable
private fun OnePlan(
    modifier: Modifier = Modifier,
    plan: PlanTypeUiState,
    textAlign: TextAlign
) {
    when (plan) {
        is PlanTypeUiState.TextRes -> {
            Text(
                modifier = modifier,
                text = stringResource(id = plan.id),
                style = ProtonTheme.typography.body1Regular.copy(
                    color = ProtonTheme.colors.textNorm
                ),
                textAlign = textAlign
            )
        }

        is PlanTypeUiState.Text -> {
            Text(
                modifier = modifier,
                text = plan.text,
                style = ProtonTheme.typography.body1Regular.copy(
                    color = ProtonTheme.colors.textNorm
                ),
                textAlign = textAlign
            )
        }

        PlanTypeUiState.Check -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(color = PassTheme.colors.textNorm)
                        .padding(all = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "check",
                        tint = PassTheme.colors.backgroundMedium
                    )
                }
            }
        }

        PlanTypeUiState.Empty -> {
            Text(
                modifier = modifier,
                text = "-",
                style = ProtonTheme.typography.body1Regular.copy(
                    color = ProtonTheme.colors.textHint
                ),
                maxLines = 1,
                textAlign = textAlign
            )
        }
    }
}

@Preview
@Composable
fun UpsellV2SectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            UpsellV2Section(
                modifier = Modifier.fillMaxWidth(),
                leftColumnText = "Free",
                rightColumnText = "Plus",
                items = elementsPlusPlan,
                weightCol1 = WEIGHT_PASS_COL1,
                weightCol2 = WEIGHT_PASS_COL2,
                weightCol3 = WEIGHT_PASS_COL3
            )
        }
    }
}

@Preview
@Composable
fun UpsellV2Section2Preview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
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
