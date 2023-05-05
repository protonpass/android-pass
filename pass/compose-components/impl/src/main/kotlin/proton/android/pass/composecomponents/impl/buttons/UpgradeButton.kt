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
import androidx.compose.ui.graphics.Color
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
    color: Color = PassTheme.colors.interactionNormMajor1,
    onUpgradeClick: () -> Unit
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp, 10.dp),
        color = color,
        onClick = onUpgradeClick
    ) {
        Text(
            text = stringResource(R.string.upgrade),
            style = PassTypography.body3Regular,
            color = PassTheme.colors.textInvert
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrow_out_square),
            contentDescription = stringResource(R.string.upgrade_icon_content_description),
            tint = PassTheme.colors.textInvert
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
