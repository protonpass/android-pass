package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun StickyTotpOptions(
    modifier: Modifier = Modifier,
    onPasteCode: () -> Unit,
    onScanCode: () -> Unit
) {
    StickyImeRow(modifier) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onPasteCode() }
                .fillMaxHeight()
                .padding(6.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_squares),
                contentDescription = stringResource(R.string.paste_code_icon_content_description),
                tint = PassTheme.colors.accentPurpleOpaque
            )
            Text(
                text = stringResource(R.string.totp_paste_code_action),
                color = PassTheme.colors.accentPurpleOpaque,
                style = ProtonTheme.typography.default,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(0.dp, 9.dp)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onScanCode() }
                .fillMaxHeight()
                .padding(6.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_camera),
                contentDescription = stringResource(R.string.scan_code_icon_content_description),
                tint = PassTheme.colors.accentPurpleOpaque
            )
            Text(
                text = stringResource(R.string.totp_scan_code_action),
                color = PassTheme.colors.accentPurpleOpaque,
                style = ProtonTheme.typography.default,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun StickyTotpOptionsPreview(
    @PreviewParameter(ThemePreviewProvider::class) input: Boolean
) {
    PassTheme(isDark = input) {
        Surface {
            StickyTotpOptions(onPasteCode = {}, onScanCode = {})
        }
    }
}
