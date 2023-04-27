package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
fun PrivacySection(
    modifier: Modifier = Modifier,
    useFavicons: Boolean,
    onUseFaviconsChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_privacy_section_title),
            style = ProtonTheme.typography.defaultSmallWeak,
        )

        Box(modifier = Modifier.roundedContainerNorm()) {
            SettingToggle(
                text = stringResource(R.string.settings_use_favicons_preference_title),
                isChecked = useFavicons,
                onClick = { onUseFaviconsChange(it) }
            )
        }

        Text(
            text = stringResource(R.string.settings_use_favicons_preference_subtitle),
            style = ProtonTheme.typography.caption.copy(PassTheme.colors.textWeak)
        )
    }
}

@Preview
@Composable
fun UseFaviconsSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PrivacySection(
                useFavicons = input.second,
                onUseFaviconsChange = {}
            )
        }
    }
}
