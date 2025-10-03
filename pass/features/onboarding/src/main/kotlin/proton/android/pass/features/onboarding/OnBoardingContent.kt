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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.flow.collectLatest
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.features.onboarding.OnBoardingPageName.Autofill
import proton.android.pass.features.onboarding.OnBoardingPageName.Fingerprint
import proton.android.pass.features.onboarding.OnBoardingPageName.InvitePending
import proton.android.pass.features.onboarding.OnBoardingPageName.Last

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingContent(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onMainButtonClick: (OnBoardingPageName) -> Unit,
    onSkipButtonClick: (OnBoardingPageName) -> Unit,
    onSelectedPageChanged: (Int) -> Unit,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { uiState.enabledPages.size })
) {
    Column(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LaunchedEffect(
            pagerState,
            uiState.selectedPage != pagerState.currentPage && !pagerState.isScrollInProgress
        ) {
            if (
                pagerState.canScrollForward &&
                pagerState.currentPage < uiState.selectedPage &&
                uiState.selectedPage < uiState.enabledPages.size
            ) {
                pagerState.animateScrollToPage(uiState.selectedPage)
            }
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collectLatest { onSelectedPageChanged(it) }
        }
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { page ->
            val pageState = when (uiState.enabledPages.getOrNull(page)) {
                null -> null
                Autofill -> autofillPageUiState()
                Fingerprint -> fingerPrintPageUiState()
                Last -> lastPageUiState()
                InvitePending -> pendingAccessPageUiState()
            }

            if (pageState != null) {
                OnBoardingPage(
                    modifier = Modifier.testTag(pageState.page.name),
                    onBoardingPageData = pageState,
                    onMainButtonClick = onMainButtonClick,
                    onSkipButtonClick = onSkipButtonClick
                )
            }
        }
        Spacer(modifier = Modifier.padding(Spacing.extraSmall))
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(Spacing.medium),
            pageCount = uiState.enabledPages.size
        )
    }
}

@Composable
fun pendingAccessPageUiState(): OnBoardingPageUiState = OnBoardingPageUiState(
    page = InvitePending,
    title = stringResource(R.string.on_boarding_pending_invite_title),
    subtitle = stringResource(R.string.on_boarding_pending_invite_content),
    image = @Composable {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(onBoardingBrush())
                .padding(38.dp, Spacing.none),
            painter = painterResource(id = R.drawable.account_setup),
            contentDescription = ""
        )
    },
    mainButton = stringResource(R.string.on_boarding_pending_invite_button),
    showSkipButton = false
)

@Composable
fun autofillPageUiState(): OnBoardingPageUiState = OnBoardingPageUiState(
    page = Autofill,
    title = stringResource(R.string.on_boarding_autofill_title),
    subtitle = stringResource(R.string.on_boarding_autofill_content),
    image = @Composable {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(onBoardingBrush())
                .padding(38.dp, Spacing.none),
            painter = painterResource(id = R.drawable.onboarding_autofill),
            contentDescription = ""
        )
    },
    mainButton = stringResource(R.string.on_boarding_autofill_button),
    showSkipButton = true
)

@Composable
private fun onBoardingBrush() = Brush.linearGradient(
    colors = listOf(
        ProtonTheme.colors.brandNorm.copy(alpha = 0.3F),
        Color.Transparent,
        Color.Transparent,
        Color.Transparent
    )
)

@Composable
fun fingerPrintPageUiState(): OnBoardingPageUiState = OnBoardingPageUiState(
    page = Fingerprint,
    title = stringResource(R.string.on_boarding_fingerprint_title),
    subtitle = stringResource(R.string.on_boarding_fingerprint_content),
    image = @Composable {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(onBoardingBrush()),
            painter = painterResource(id = R.drawable.onboarding_fingerprint),
            contentDescription = ""
        )
    },
    mainButton = stringResource(R.string.on_boarding_fingerprint_button),
    showSkipButton = true
)

@Composable
fun lastPageUiState(): OnBoardingPageUiState = OnBoardingPageUiState(
    page = Last,
    title = stringResource(R.string.on_boarding_last_page_title),
    subtitle = stringResource(R.string.on_boarding_last_page_content),
    image = @Composable {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(onBoardingBrush()),
            painter = painterResource(id = R.drawable.onboarding_last),
            contentDescription = ""
        )
    },
    mainButton = stringResource(R.string.on_boarding_last_page_button),
    showSkipButton = false,
    showVideoTutorialButton = true
)

class ThemeAndOnBoardingUiStatePreviewProvider :
    ThemePairPreviewProvider<OnBoardingUiState>(OnBoardingUiStatePreviewProvider())

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun OnBoardingContentPreview(
    @PreviewParameter(ThemeAndOnBoardingUiStatePreviewProvider::class)
    input: Pair<Boolean, OnBoardingUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            OnBoardingContent(Modifier, input.second, {}, {}, {})
        }
    }
}
