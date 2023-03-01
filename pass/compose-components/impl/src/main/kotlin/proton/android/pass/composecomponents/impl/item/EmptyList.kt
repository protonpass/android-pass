package proton.android.pass.composecomponents.impl.item

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.theme.headline
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.CircleButton

@Composable
fun EmptyList(
    modifier: Modifier = Modifier,
    emptyListMessage: String,
    emptyListTitle: String = stringResource(R.string.empty_list_title),
    @DrawableRes emptyListImage: Int = R.drawable.placeholder_bound_box,
    onCreateItemClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = emptyListImage),
            contentDescription = stringResource(R.string.empty_list_image_content_description)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = emptyListTitle,
            style = ProtonTheme.typography.headline,
            textAlign = TextAlign.Center
        )
        Text(
            text = emptyListMessage,
            style = ProtonTheme.typography.defaultWeak,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onCreateItemClick != null) {
                CircleButton(
                    contentPadding = ButtonDefaults.ContentPadding,
                    color = PassTheme.colors.accentBrandOpaque,
                    onClick = onCreateItemClick
                ) {
                    Text(
                        text = stringResource(R.string.empty_list_create_item_button),
                        style = PassTypography.body3Regular,
                        color = ProtonTheme.colors.textNorm
                    )
                }
            }
            val context = LocalContext.current
            CircleButton(
                contentPadding = ButtonDefaults.ContentPadding,
                color = PassTheme.colors.accentBrandWeakest,
                onClick = {
                    BrowserUtils.openWebsite(
                        context = context,
                        website = EXTENSION_URL
                    )
                }
            ) {
                Text(
                    text = stringResource(R.string.empty_list_open_extension),
                    style = PassTypography.body3Regular,
                    color = PassTheme.colors.accentBrandOpaque
                )
            }
        }
    }
}

private const val EXTENSION_URL =
    "https://chrome.google.com/webstore/detail/proton-pass/ghmbeldphafepmbegfdlkpapadhbakde"

@Preview
@Composable
fun EmptListPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            EmptyList(
                emptyListMessage = "Create a new item",
                onCreateItemClick = {}
            )
        }
    }
}
