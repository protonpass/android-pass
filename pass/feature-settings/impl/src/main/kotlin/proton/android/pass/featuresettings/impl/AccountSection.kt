package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonSettingsHeader
import me.proton.core.compose.component.ProtonSettingsItem
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun AccountSection(
    modifier: Modifier = Modifier,
    currentAccount: String,
    onLogoutClick: () -> Unit
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        ProtonSettingsHeader(title = stringResource(R.string.settings_account_section_title))
        ProtonSettingsItem(
            name = stringResource(R.string.settings_account_log_out_title),
            hint = stringResource(R.string.settings_account_current_account, currentAccount),
            onClick = onLogoutClick
        )
    }
}

@Preview
@Composable
fun AccountSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountSection(
                currentAccount = "preview@user.test",
                onLogoutClick = {}
            )
        }
    }
}

