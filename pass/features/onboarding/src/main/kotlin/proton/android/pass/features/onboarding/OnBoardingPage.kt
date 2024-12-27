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

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleButton

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    onBoardingPageData: OnBoardingPageUiState,
    onMainButtonClick: (OnBoardingPageName) -> Unit,
    onSkipButtonClick: (OnBoardingPageName) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        onBoardingPageData.image(this)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                modifier = Modifier.padding(32.dp, Spacing.none),
                color = ProtonTheme.colors.textNorm,
                style = ProtonTheme.typography.headlineNorm,
                text = onBoardingPageData.title,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(32.dp, Spacing.none),
                text = onBoardingPageData.subtitle,
                style = ProtonTheme.typography.defaultWeak,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircleButton(
                modifier = Modifier
                    .testTag(OnBoardingPageTestTag.mainButton)
                    .padding(32.dp, Spacing.none)
                    .fillMaxWidth()
                    .height(48.dp),
                color = PassTheme.colors.interactionNormMajor1,
                onClick = { onMainButtonClick(onBoardingPageData.page) }
            ) {
                Text(
                    text = onBoardingPageData.mainButton,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (onBoardingPageData.showSkipButton) {
                ProtonTextButton(
                    modifier = Modifier
                        .testTag(OnBoardingPageTestTag.skipButton)
                        .padding(32.dp, Spacing.none)
                        .fillMaxWidth()
                        .height(48.dp),
                    onClick = { onSkipButtonClick(onBoardingPageData.page) }
                ) {
                    Text(
                        text = stringResource(R.string.on_boarding_skip),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            if (onBoardingPageData.showVideoTutorialButton) {
                val context = LocalContext.current
                ProtonTextButton(
                    modifier = Modifier
                        .padding(32.dp, Spacing.none)
                        .fillMaxWidth(),
                    onClick = { openWebsite(context, PASS_TUTORIAL) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                    ) {
                        Text(
                            text = stringResource(R.string.on_boarding_tutorial),
                            textAlign = TextAlign.Center,
                            style = ProtonTheme.typography.defaultSmallNorm,
                            color = PassTheme.colors.interactionNormMajor1
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_pass_youtube),
                            contentDescription = null,
                            tint = PassTheme.colors.interactionNormMajor1
                        )
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .heightIn(Spacing.none, 50.dp)
                    .weight(1f)
            )
        }
    }
}

@VisibleForTesting
const val PASS_TUTORIAL = "https://www.youtube.com/watch?v=Nm4DCAjePOM"

object OnBoardingPageTestTag {
    const val mainButton = "mainButton"
    const val skipButton = "skipButton"
}

