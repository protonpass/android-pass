/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureonboarding.impl

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Autofill
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Fingerprint
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Last

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingContent(
    modifier: Modifier = Modifier,
    uiState: OnBoardingUiState,
    onMainButtonClick: (OnBoardingPageName) -> Unit,
    onSkipButtonClick: (OnBoardingPageName) -> Unit,
    onSelectedPageChanged: (Int) -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    pagerState: PagerState = rememberPagerState(initialPage = 0)
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        LaunchedEffect(uiState.selectedPage != pagerState.currentPage && !pagerState.isScrollInProgress) {
            coroutineScope.launch { pagerState.animateScrollToPage(uiState.selectedPage) }
        }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collectLatest { onSelectedPageChanged(it) }
        }
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
            pageCount = uiState.enabledPages.size
        ) { page ->
            val pageUiState = if (uiState.enabledPages.contains(Autofill) && page == 0) {
                autofillPageUiState()
            } else if (
                isFingerprintSecondPage(uiState, page) || isFingerprintFirstPage(uiState, page)
            ) {
                fingerPrintPageUiState()
            } else {
                lastPageUiState()
            }
            OnBoardingPage(
                onBoardingPageData = pageUiState,
                onMainButtonClick = onMainButtonClick,
                onSkipButtonClick = onSkipButtonClick
            )
        }
        Spacer(modifier = Modifier.padding(4.dp))
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            pageCount = uiState.enabledPages.size
        )
    }
}

@Composable
private fun isFingerprintSecondPage(
    uiState: OnBoardingUiState,
    page: Int
) = uiState.enabledPages.containsAll(listOf(Fingerprint, Autofill)) && page == 1

@Composable
private fun isFingerprintFirstPage(
    uiState: OnBoardingUiState,
    page: Int
) = uiState.enabledPages.contains(Fingerprint) &&
    !uiState.enabledPages.contains(Autofill) &&
    page == 0

@Composable
fun autofillPageUiState(): OnBoardingPageUiState =
    OnBoardingPageUiState(
        page = Autofill,
        title = stringResource(R.string.on_boarding_autofill_title),
        subtitle = stringResource(R.string.on_boarding_autofill_content),
        image = @Composable {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(onBoardingBrush())
                    .padding(38.dp, 0.dp),
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
fun fingerPrintPageUiState(): OnBoardingPageUiState =
    OnBoardingPageUiState(
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
fun lastPageUiState(): OnBoardingPageUiState =
    OnBoardingPageUiState(
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
        showSkipButton = false
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
