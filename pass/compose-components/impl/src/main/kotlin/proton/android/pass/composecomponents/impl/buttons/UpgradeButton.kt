package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun UpgradeButton(
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp, 10.dp),
        color = PassTheme.colors.interactionNormMinor2,
        onClick = onUpgradeClick
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.drawable.ic_brand_pass),
            contentDescription = stringResource(R.string.upgrade_icon_content_description),
            tint = PassTheme.colors.interactionNorm
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.upgrade),
            style = PassTypography.body3Regular,
            color = PassTheme.colors.interactionNorm
        )
    }
}

@Preview
@Composable
fun UpgradeButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            UpgradeButton {}
        }
    }
}
