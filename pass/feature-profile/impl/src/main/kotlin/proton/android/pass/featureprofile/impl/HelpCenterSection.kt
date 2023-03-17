package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer

@Composable
fun HelpCenterProfileSection(
    modifier: Modifier = Modifier,
    onTipsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onRateAppClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.profile_help_center),
            style = ProtonTheme.typography.defaultSmallWeak,
            color = PassTheme.colors.textWeak
        )
        Column(
            modifier = Modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
        ) {
            ProfileOption(
                text = stringResource(R.string.profile_option_tips),
                onClick = onTipsClick
            )
            Divider()
            ProfileOption(
                text = stringResource(R.string.profile_option_feedback),
                onClick = onFeedbackClick
            )
            Divider()
            ProfileOption(
                text = stringResource(R.string.profile_option_rating),
                onClick = onRateAppClick
            )
        }
    }
}

@Preview
@Composable
fun HelpCenterSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            HelpCenterProfileSection(
                onTipsClick = {},
                onFeedbackClick = {},
                onRateAppClick = {}
            )
        }
    }
}
