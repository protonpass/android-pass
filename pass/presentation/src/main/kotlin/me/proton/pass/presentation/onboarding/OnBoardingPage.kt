package me.proton.pass.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import me.proton.core.compose.theme.defaultWeak
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        onBoardingPageData.image(this)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                modifier = Modifier.padding(32.dp, 0.dp),
                color = ProtonTheme.colors.textNorm,
                style = ProtonTheme.typography.headline,
                text = onBoardingPageData.title,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.padding(32.dp, 0.dp),
                text = onBoardingPageData.subtitle,
                style = ProtonTheme.typography.defaultWeak,
                textAlign = TextAlign.Center
            )
            Spacer(
                modifier = Modifier
                    .height(24.dp)
                    .weight(1f)
            )
            ProtonSolidButton(
                modifier = Modifier
                    .padding(32.dp, 0.dp)
                    .fillMaxWidth()
                    .height(48.dp),
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
                        .padding(32.dp, 0.dp)
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
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
            Spacer(
                modifier = Modifier
                    .heightIn(0.dp, 50.dp)
                    .weight(1f)
            )
        }
    }
}
