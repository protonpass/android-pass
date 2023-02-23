package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.featurecreateitem.impl.R

@Composable
fun StickyUsernameOptions(
    modifier: Modifier = Modifier,
    primaryEmail: String?,
    onCreateAliasClick: () -> Unit,
    onPrefillCurrentEmailClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black),
    ) {
        val halfWeight = 1 / 2f
        Row(
            modifier = Modifier
                .weight(halfWeight)
                .clickable { onCreateAliasClick() }
                .fillMaxHeight()
                .padding(6.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
                contentDescription = stringResource(R.string.sticky_button_create_alias_icon_content_description),
                tint = PassTheme.colors.accentPurpleOpaque
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.sticky_button_create_alias),
                color = PassTheme.colors.accentPurpleOpaque,
                style = ProtonTheme.typography.default,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        if (primaryEmail != null) {
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(0.dp, 9.dp)
            )
            Row(
                modifier = Modifier
                    .weight(halfWeight)
                    .clickable { onPrefillCurrentEmailClick(primaryEmail) }
                    .fillMaxHeight()
                    .padding(6.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        id = R.string.sticky_button_use_account_email,
                        primaryEmail
                    ),
                    color = PassTheme.colors.accentPurpleOpaque,
                    style = ProtonTheme.typography.default,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
    }
}

class ThemedStickyUsernamePreviewProvider :
    ThemePairPreviewProvider<Option<String>>(StickyUsernameOptionsPreviewProvider())

@Preview
@Composable
fun StickyUsernameOptionsPreview(
    @PreviewParameter(ThemedStickyUsernamePreviewProvider::class) input: Pair<Boolean, Option<String>>
) {
    PassTheme(isDark = input.first) {
        Surface {
            StickyUsernameOptions(
                primaryEmail = input.second.value(),
                onCreateAliasClick = {},
                onPrefillCurrentEmailClick = {}
            )
        }
    }
}
