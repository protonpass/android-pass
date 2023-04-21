package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon

@Composable
internal fun ItemRow(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    title: AnnotatedString,
    subtitles: ImmutableList<AnnotatedString>,
    vaultIcon: Int?
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        icon()
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                vaultIcon?.let {
                    Icon(
                        modifier = Modifier.height(12.dp),
                        painter = painterResource(it),
                        contentDescription = stringResource(R.string.vault_icon_content_description),
                        tint = PassTheme.colors.textWeak
                    )
                }
                Text(
                    text = title,
                    style = ProtonTheme.typography.defaultNorm,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            subtitles.filter { it.isNotBlank() }
                .forEach {
                    Text(
                        text = it,
                        style = ProtonTheme.typography.defaultSmallWeak,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
        }
    }
}

@Preview
@Composable
fun ItemRowPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemRow(
                icon = { NoteIcon() },
                title = "title".asAnnotatedString(),
                subtitles = persistentListOf("".asAnnotatedString()),
                vaultIcon = null
            )
        }
    }
}
