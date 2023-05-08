package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@Composable
fun TotpLimit(
    modifier: Modifier = Modifier,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_lock),
            contentDescription = stringResource(R.string.mfa_icon_content_description),
            tint = ProtonTheme.colors.iconWeak
        )
        Column {
            ProtonTextFieldLabel(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(id = proton.android.pass.featureitemcreate.impl.R.string.mfa_limit_reached),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onUpgrade)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(proton.android.pass.featureitemcreate.impl.R.string.upgrade),
                    style = ProtonTheme.typography.defaultNorm,
                    color = PassTheme.colors.interactionNormMajor2
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                    contentDescription = stringResource(ComponentsR.string.upgrade_icon_content_description),
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}


@Preview
@Composable
fun TotpLimitPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TotpLimit {}
        }
    }
}
