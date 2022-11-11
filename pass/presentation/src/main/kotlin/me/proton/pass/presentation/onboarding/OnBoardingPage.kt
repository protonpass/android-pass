package me.proton.pass.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.headline
import me.proton.pass.presentation.R

@Composable
fun OnBoardingPage(
    modifier: Modifier = Modifier,
    onBoardingPageData: OnBoardingPageUiState,
    onMainButtonClick: (OnBoardingPageName) -> Unit,
    onSkipButtonClick: (OnBoardingPageName) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            color = ProtonTheme.colors.textNorm,
            style = ProtonTheme.typography.headline,
            text = onBoardingPageData.title,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = onBoardingPageData.subtitle,
            style = ProtonTypography.Default.default,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(58.dp))
        ProtonSolidButton(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = { onMainButtonClick(onBoardingPageData.page) }
        ) {
            Text(
                text = onBoardingPageData.mainButton,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        ProtonTextButton(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = { onSkipButtonClick(onBoardingPageData.page) }
        ) {
            Text(
                text = stringResource(R.string.on_boarding_skip),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
