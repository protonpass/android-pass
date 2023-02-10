package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer
import proton.android.pass.featureitemdetail.impl.DetailSectionSubtitle
import proton.android.pass.featureitemdetail.impl.DetailSectionTitle
import proton.android.pass.featureitemdetail.impl.R

@Composable
fun AliasAddressSection(
    modifier: Modifier = Modifier,
    alias: String,
    onAliasCopied: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    RoundedCornersContainer(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            clipboardManager.setText(AnnotatedString(alias))
            onAliasCopied()
        }
    ) {
        Column {
            DetailSectionTitle(text = stringResource(R.string.field_alias_title))
            Spacer(modifier = Modifier.height(8.dp))
            DetailSectionSubtitle(text = alias)
        }
    }
}

@Preview
@Composable
fun AliasAddressSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AliasAddressSection(
                alias = "some@alias.local",
                onAliasCopied = {}
            )
        }
    }
}
