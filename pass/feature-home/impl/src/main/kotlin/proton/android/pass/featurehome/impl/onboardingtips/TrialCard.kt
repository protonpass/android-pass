package proton.android.pass.featurehome.impl.onboardingtips

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featurehome.impl.R

@Composable
fun TrialCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    SpotlightCard(
        modifier = modifier,
        backgroundColor = PassTheme.colors.noteInteractionNormMajor1,
        title = stringResource(id = R.string.home_trial_banner_title),
        body = stringResource(id = R.string.home_trial_banner_text),
        buttonText = stringResource(id = R.string.home_trial_banner_settings),
        onClick = onClick,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun TrialCardPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialCard(
                onClick = {},
                onDismiss = {}
            )
        }
    }
}
