package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun StickyGeneratePassword(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    StickyImeRow(
        modifier = modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_key),
            contentDescription = stringResource(R.string.sticky_button_generate_password_key_icon_content_description),
            tint = PassTheme.colors.accentPurpleOpaque
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.sticky_button_generate_password),
            color = PassTheme.colors.accentPurpleOpaque,
            style = ProtonTheme.typography.default,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun StickyGeneratePasswordPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            StickyGeneratePassword {}
        }
    }
}
