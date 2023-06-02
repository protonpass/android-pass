package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import me.proton.core.presentation.R as CoreR

@Composable
fun TrialFeatureRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    feature: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Text(
            text = feature,
            style = ProtonTheme.typography.defaultNorm
        )
    }
}

@Preview
@Composable
fun TrialFeatureRowPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialFeatureRow(
                icon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_lock),
                        contentDescription = null,
                        tint = PassTheme.colors.loginInteractionNorm
                    )
                },
                feature = "Some random feature"
            )
        }
    }
}
