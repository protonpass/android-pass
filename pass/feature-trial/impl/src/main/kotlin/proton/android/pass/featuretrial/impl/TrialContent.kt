package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrialContent(
    modifier: Modifier = Modifier,
    state: TrialUiState,
    onNavigate: (TrialNavigation) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.trial),
            contentDescription = stringResource(R.string.trial_image_content_description)
        )
        Spacer(modifier = Modifier.height(52.dp))

        Text(
            text = stringResource(R.string.trial_title),
            style = PassTypography.hero
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.trial_subtitle),
            style = ProtonTheme.typography.defaultNorm
        )
        Spacer(modifier = Modifier.height(35.dp))

        TrialFeatures()

        Spacer(modifier = Modifier.height(28.dp))

        TrialGradientButton(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.trial_button_text),
            onClick = { onNavigate(TrialNavigation.Upgrade) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pluralStringResource(
                R.plurals.trial_days_left,
                state.remainingTrialDays,
                state.remainingTrialDays
            ),
            style = PassTypography.body3Regular
        )

        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onNavigate(TrialNavigation.LearnMore)
                }
                .padding(8.dp),
            text = stringResource(R.string.trial_learn_more),
            color = PassTheme.colors.interactionNormMajor2,
            style = PassTypography.body3Regular.copy(textDecoration = TextDecoration.Underline)
        )
    }
}

@Preview
@Composable
fun TrialContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialContent(
                state = TrialUiState(remainingTrialDays = 1),
                onNavigate = {}
            )
        }
    }
}
