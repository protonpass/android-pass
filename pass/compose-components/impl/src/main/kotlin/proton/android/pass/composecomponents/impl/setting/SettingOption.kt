package proton.android.pass.composecomponents.impl.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.placeholder

@Composable
fun SettingOption(
    modifier: Modifier = Modifier,
    text: String,
    label: String? = null,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .applyIf(onClick != null, ifTrue = { clickable { onClick?.invoke() } })
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            label?.let {
                Text(
                    text = it,
                    style = PassTypography.body3RegularWeak,
                )
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(condition = isLoading, ifTrue = { placeholder() }),
                text = text,
                style = ProtonTheme.typography.defaultWeak,
                color = PassTheme.colors.textNorm
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_tiny_right),
            contentDescription = stringResource(R.string.setting_option_icon_content_description),
            tint = PassTheme.colors.textHint
        )
    }
}

@Preview
@Composable
fun SettingOptionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SettingOption(text = "Match system", label = "Theme", onClick = {})
        }
    }
}
