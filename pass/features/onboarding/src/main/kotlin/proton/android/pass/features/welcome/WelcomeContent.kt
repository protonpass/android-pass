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

package proton.android.pass.features.welcome

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.Gradients
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.onboarding.R

val onboardingSlides = listOf(
    Triple(
        R.string.onboarding_slide_1_title,
        R.string.onboarding_slide_1_subtitle,
        R.drawable.onboarding_slide_1
    ),
    Triple(
        R.string.onboarding_slide_2_title,
        R.string.onboarding_slide_2_subtitle,
        R.drawable.onboarding_slide_2
    ),
    Triple(
        R.string.onboarding_slide_3_title,
        R.string.onboarding_slide_3_subtitle,
        R.drawable.onboarding_slide_3
    ),
    Triple(
        R.string.onboarding_slide_4_title,
        R.string.onboarding_slide_4_subtitle,
        R.drawable.onboarding_slide_4
    )
)

@Composable
internal fun WelcomeContent(
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { onboardingSlides.size }
    ),
    onSignUp: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Gradients.RadialOnboarding
            )
            .systemBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LineIndicator(
            modifier = Modifier.padding(top = Spacing.medium),
            pagerState = pagerState
        )
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val screenWidth = size.width
                        coroutineScope.launch {
                            if (offset.x < screenWidth / 2) {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            } else {
                                if (pagerState.currentPage < pagerState.pageCount - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                    }
                }
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState
            ) { page ->
                val (titleRes, subtitleRes, imageRes) = onboardingSlides[page]
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isLandscape) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text.Hero(
                        modifier = Modifier.padding(horizontal = Spacing.medium),
                        text = stringResource(titleRes),
                        color = PassPalette.White100
                    )
                    Text.Subheadline(
                        modifier = Modifier.padding(horizontal = Spacing.medium),
                        text = stringResource(subtitleRes),
                        textAlign = TextAlign.Center,
                        color = PassPalette.White100
                    )
                    Spacer(modifier = Modifier.height(height = Spacing.medium))
                    Spacer(modifier = Modifier.weight(1f))
                    Image.Default(
                        id = imageRes,
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Button.Circular(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
                    color = PassPalette.Indigo,
                    elevation = ButtonDefaults.elevation(0.dp),
                    onClick = onSignUp
                ) {
                    Text.Body1Medium(
                        modifier = Modifier.padding(Spacing.small),
                        text = stringResource(R.string.create_an_account_button),
                        color = PassPalette.White100
                    )
                }
                Button.Circular(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium),
                    borderStroke = BorderStroke(1.dp, PassPalette.White100),
                    color = Color.Transparent,
                    elevation = ButtonDefaults.elevation(0.dp),
                    onClick = onSignIn
                ) {
                    Text.Body1Medium(
                        modifier = Modifier.padding(Spacing.small),
                        text = stringResource(R.string.sign_in_button),
                        color = PassPalette.White100
                    )
                }
            }
        }
        Image.Default(
            modifier = Modifier.padding(Spacing.medium),
            id = R.drawable.proton_privacy_by_default_small
        )
    }
}

@Preview
@Composable
fun WelcomeContentSlide0Preview() {
    PassTheme(isDark = true) {
        Surface {
            WelcomeContent(
                pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { onboardingSlides.size }
                ),
                onSignUp = {},
                onSignIn = {}
            )
        }
    }
}

@Preview
@Composable
fun WelcomeContentSlide1Preview() {
    PassTheme(isDark = true) {
        Surface {
            WelcomeContent(
                pagerState = rememberPagerState(
                    initialPage = 1,
                    pageCount = { onboardingSlides.size }
                ),
                onSignUp = {},
                onSignIn = {}
            )
        }
    }
}

@Preview
@Composable
fun WelcomeContentSlide2Preview() {
    PassTheme(isDark = true) {
        Surface {
            WelcomeContent(
                pagerState = rememberPagerState(
                    initialPage = 2,
                    pageCount = { onboardingSlides.size }
                ),
                onSignUp = {},
                onSignIn = {}
            )
        }
    }
}

@Preview
@Composable
fun WelcomeContentSlide3Preview() {
    PassTheme(isDark = true) {
        Surface {
            WelcomeContent(
                pagerState = rememberPagerState(
                    initialPage = 3,
                    pageCount = { onboardingSlides.size }
                ),
                onSignUp = {},
                onSignIn = {}
            )
        }
    }
}
