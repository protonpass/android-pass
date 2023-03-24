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
fun DeleteAccount(modifier: Modifier = Modifier, onDeleteAccountClick: () -> Unit) {
    ColorSettingOption(
        modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm),
        text = stringResource(R.string.account_delete_account),
        textColor = PassTheme.colors.passwordInteractionNormMajor1,
        iconBgColor = PassTheme.colors.passwordInteractionNormMinor2,
        icon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_trash),
                contentDescription = stringResource(R.string.account_delete_account_icon_content_description),
                tint = PassTheme.colors.passwordInteractionNormMajor1
            )
        },
        onClick = onDeleteAccountClick
    )
}

@Preview
@Composable
fun DeleteAccountPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            DeleteAccount {}
        }
    }
}
