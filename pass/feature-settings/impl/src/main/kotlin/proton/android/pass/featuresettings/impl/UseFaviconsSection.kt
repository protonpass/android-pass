package proton.android.pass.featuresettings.impl

import androidx.compose.foundation.layout.Arrangement
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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingToggle

@Composable
fun UseFaviconsSection(
    modifier: Modifier = Modifier,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingToggle(
            modifier = Modifier.roundedContainerNorm(),
            text = stringResource(R.string.settings_use_favicons_preference_title),
            isChecked = value,
            onClick = { onChange(it) }
        )
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
            UseFaviconsSection(
                value = input.second,
                onChange = {}
            )
        }
    }
}
