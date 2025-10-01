/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.buttons.PassOutlinedButton

@Composable
fun OnBoardingPageV2(
    modifier: Modifier = Modifier,
    onBoardingPageData: OnBoardingPageUiState,
    onMainButtonClick: (OnBoardingPageName) -> Unit,
    onSkipButtonClick: (OnBoardingPageName) -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                onBoardingPageData.image(this@Column)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.padding(Spacing.mediumLarge, Spacing.none),
                color = ProtonTheme.colors.textNorm,
                style = ProtonTheme.typography.hero,
                text = onBoardingPageData.title,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(Spacing.mediumLarge, Spacing.none),
                text = onBoardingPageData.subtitle,
                color = ProtonTheme.colors.textNorm.copy(
                    alpha = 0.7f
                ),
                style = ProtonTheme.typography.subheadline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // buttons
            CircleButton(
                modifier = Modifier
                    .testTag(OnBoardingPageTestTag.mainButton)
                    .padding(Spacing.mediumLarge, Spacing.none)
                    .fillMaxWidth()
                    .height(52.dp),
                color = PassTheme.colors.interactionNormMajor1,
                onClick = { onMainButtonClick(onBoardingPageData.page) }
            ) {
                Text(
                    text = onBoardingPageData.mainButton,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = PassTheme.colors.interactionNormContrast,
                    style = ProtonTheme.typography.body1Regular
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (onBoardingPageData.showVideoTutorialButton) {
                val context = LocalContext.current
                PassOutlinedButton(
                    modifier = Modifier
                        .padding(Spacing.mediumLarge, Spacing.none)
                        .fillMaxWidth()
                        .height(52.dp),
                    backgroundColor = Color.Transparent,
                    color = ProtonTheme.colors.textNorm,
                    style = ProtonTheme.typography.body1Regular,
                    borderColor = ProtonTheme.colors.textNorm.copy(alpha = 0.7f),
                    onClick = { openWebsite(context, PASS_TUTORIAL) },
                    text = stringResource(R.string.on_boarding_tutorial_v2),
                    shape = CircleShape,
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(52.dp))
            }

        }

        if (onBoardingPageData.showSkipButton) {
            ProtonTextButton(
                modifier = Modifier
                    .testTag(OnBoardingPageTestTag.skipButton)
                    .padding(Spacing.mediumLarge, Spacing.none)
                    .align(alignment = Alignment.TopEnd)
                    .statusBarsPadding()
                    .height(48.dp),
                onClick = { onSkipButtonClick(onBoardingPageData.page) }
            ) {
                Text(
                    text = stringResource(R.string.on_boarding_skip),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = ProtonTheme.colors.textNorm,
                    style = ProtonTheme.typography.body1Regular
                )
            }
        }
    }
}
