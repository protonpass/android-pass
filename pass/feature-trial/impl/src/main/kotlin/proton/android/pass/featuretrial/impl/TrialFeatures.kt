package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider

@Composable
fun TrialFeatures(
    modifier: Modifier = Modifier
) {
    RoundedCornersColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TrialFeatureRow(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
            feature = stringResource(R.string.trial_feature_1),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.trial_multiple_vaults),
                    contentDescription = stringResource(R.string.trial_feature_1),
                    tint = Color.Unspecified
                )
            }
        )
        PassDivider()
        TrialFeatureRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            feature = stringResource(R.string.trial_feature_2),
            icon = {
                Icon(
                    modifier = Modifier.padding(start = 4.dp), // Needed to match alignment with 1
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_lock),
                    contentDescription = stringResource(R.string.trial_feature_2),
                    tint = PassTheme.colors.loginInteractionNorm
                )
            }
        )
        PassDivider()
        TrialFeatureRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            feature = stringResource(R.string.trial_feature_3),
            icon = {
                Icon(
                    modifier = Modifier.padding(start = 4.dp), // Needed to match alignment with 1
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_list_bullets),
                    contentDescription = stringResource(R.string.trial_feature_3),
                    tint = PassTheme.colors.aliasInteractionNormMajor2
                )
            }
        )
        PassDivider()

    }
}

@Preview
@Composable
fun TrialFeaturesPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialFeatures()
        }
    }
}
