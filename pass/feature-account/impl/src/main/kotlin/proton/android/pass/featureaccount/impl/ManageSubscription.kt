package proton.android.pass.featureaccount.impl

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption

@Composable
fun ManageSubscription(
    modifier: Modifier = Modifier,
    onManageSubscriptionClick: () -> Unit
) {
    ColorSettingOption(
        modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm),
        text = stringResource(R.string.account_manage_subscription),
        textColor = PassTheme.colors.accentBrandOpaque,
        iconBgColor = PassTheme.colors.accentBrandWeakest,
        icon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrow_out_square),
                contentDescription = stringResource(R.string.manage_subscription_icon_content_description),
                tint = PassTheme.colors.accentBrandOpaque
            )
        },
        onClick = onManageSubscriptionClick
    )
}

@Preview
@Composable
fun ManageSubscriptionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ManageSubscription {}
        }
    }
}
