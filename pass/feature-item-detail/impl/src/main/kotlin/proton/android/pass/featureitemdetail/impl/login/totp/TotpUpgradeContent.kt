package proton.android.pass.featureitemdetail.impl.login.totp

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
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.featureitemdetail.impl.R
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun TotpUpgradeContent(
    modifier: Modifier = Modifier,
    onUpgrade: () -> Unit
) {
    Row(
        modifier = modifier.padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_lock),
            contentDescription = stringResource(R.string.mfa_icon_content_description),
            tint = PassTheme.colors.loginInteractionNorm
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            SectionTitle(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(id = R.string.mfa_limit_reached),
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
                    text = stringResource(CompR.string.upgrade),
                    style = ProtonTheme.typography.defaultNorm,
                    color = PassTheme.colors.interactionNormMajor2
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                    contentDescription = stringResource(CompR.string.upgrade_icon_content_description),
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}


@Preview
@Composable
fun TotpUpgradeContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TotpUpgradeContent {}
        }
    }
}
