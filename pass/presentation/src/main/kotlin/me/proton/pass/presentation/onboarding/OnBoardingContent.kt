package me.proton.pass.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Autofill
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Fingerprint

@OptIn(ExperimentalPagerApi::class)
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
        if (uiState.enabledPages.size > 1) {
            LaunchedEffect(uiState.selectedPage != pagerState.currentPage && !pagerState.isScrollInProgress) {
                coroutineScope.launch { pagerState.animateScrollToPage(uiState.selectedPage) }
            }
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collectLatest { onSelectedPageChanged(it) }
            }
            HorizontalPager(
                modifier = Modifier,
                state = pagerState,
                count = uiState.enabledPages.size
            ) { page ->
                val pageUiState = when (page) {
                    0 -> autofillPageUiState()
                    1 -> fingerPrintPageUiState()
                    else -> throw NotImplementedError("On boarding page not implemented")
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
                    .padding(16.dp)
            )
        } else if (uiState.enabledPages.contains(Fingerprint)) {
            OnBoardingPage(
                onBoardingPageData = fingerPrintPageUiState(),
                onMainButtonClick = onMainButtonClick,
                onSkipButtonClick = onSkipButtonClick
            )
        } else if (uiState.enabledPages.contains(Autofill)) {
            OnBoardingPage(
                onBoardingPageData = autofillPageUiState(),
                onMainButtonClick = onMainButtonClick,
                onSkipButtonClick = onSkipButtonClick
            )
        }
    }
}

@Composable
fun autofillPageUiState(): OnBoardingPageUiState =
    OnBoardingPageUiState(
        page = Autofill,
        title = stringResource(R.string.on_boarding_autofill_title),
        subtitle = stringResource(R.string.on_boarding_autofill_content),
        mainButton = stringResource(R.string.on_boarding_autofill_button)
    )

@Composable
fun fingerPrintPageUiState(): OnBoardingPageUiState =
    OnBoardingPageUiState(
        page = Fingerprint,
        title = stringResource(R.string.on_boarding_fingerprint_title),
        subtitle = stringResource(R.string.on_boarding_fingerprint_content),
        mainButton = stringResource(R.string.on_boarding_fingerprint_button)
    )

class ThemeAndOnBoardingUiStatePreviewProvider :
    ThemePairPreviewProvider<OnBoardingUiState>(OnBoardingUiStatePreviewProvider())

@OptIn(ExperimentalPagerApi::class)
@Preview
@Composable
fun OnBoardingContentPreview(
    @PreviewParameter(ThemeAndOnBoardingUiStatePreviewProvider::class)
    input: Pair<Boolean, OnBoardingUiState>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            OnBoardingContent(Modifier, input.second, {}, {}, {})
        }
    }
}
