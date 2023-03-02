package proton.android.pass.featurehome.impl

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.EmptyList

@Composable
fun HomeEmptyList(
    modifier: Modifier = Modifier,
    onCreateItemClick: () -> Unit,
) {
    val context = LocalContext.current
    EmptyList(
        modifier = modifier,
        emptyListMessage = stringResource(id = R.string.empty_list_home_subtitle),
        onCreateItemClick = onCreateItemClick,
        onOpenWebsiteClick = {
            BrowserUtils.openWebsite(
                context = context,
                website = EXTENSION_URL
            )
        }
    )
}


private const val EXTENSION_URL =
    "https://chrome.google.com/webstore/detail/proton-pass/ghmbeldphafepmbegfdlkpapadhbakde"

@Preview
@Composable
fun HomeEmptyListPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeEmptyList(onCreateItemClick = {})
        }
    }
}
