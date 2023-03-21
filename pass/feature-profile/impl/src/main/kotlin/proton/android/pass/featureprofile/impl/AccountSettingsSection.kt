package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.setting.SettingOption

@Composable
fun AccountProfileSection(
    modifier: Modifier = Modifier,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = modifier.roundedContainer(ProtonTheme.colors.separatorNorm)
    ) {
        SettingOption(
            text = stringResource(R.string.profile_option_account),
            onClick = onAccountClick
        )
        Divider()
        SettingOption(
            text = stringResource(R.string.profile_option_settings),
            onClick = onSettingsClick
        )
    }
}

@Preview
@Composable
fun AccountSettingsSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountProfileSection(Modifier, {}, {})
        }
    }
}
