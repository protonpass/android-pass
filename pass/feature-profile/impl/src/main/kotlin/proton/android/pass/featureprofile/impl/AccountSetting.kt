package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R

@Composable
fun AccountSetting(
    modifier: Modifier = Modifier,
    planInfo: PlanInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp, 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(proton.android.pass.featureprofile.impl.R.string.profile_option_account),
            style = ProtonTheme.typography.defaultWeak,
            color = PassTheme.colors.textNorm
        )

        Spacer(modifier = Modifier.weight(1f))

        PlanInfoIndicator(planInfo = planInfo)

        Icon(
            painter = painterResource(R.drawable.ic_chevron_tiny_right),
            contentDescription = stringResource(R.string.setting_option_icon_content_description),
            tint = PassTheme.colors.textHint
        )
    }
}


