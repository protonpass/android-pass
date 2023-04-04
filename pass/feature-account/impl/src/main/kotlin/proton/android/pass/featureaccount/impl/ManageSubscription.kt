package proton.android.pass.featureaccount.impl

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption

@Composable
fun ManageSubscription(
    modifier: Modifier = Modifier,
    onManageSubscriptionClick: () -> Unit
) {
    ColorSettingOption(
        modifier = modifier.roundedContainerNorm(),
        text = stringResource(R.string.account_manage_subscription),
        textColor = PassTheme.colors.interactionNorm,
        iconBgColor = PassTheme.colors.interactionNormMinor2,
        icon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrow_out_square),
                contentDescription = stringResource(R.string.manage_subscription_icon_content_description),
                tint = PassTheme.colors.interactionNorm
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
