package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle

@Composable
fun NoteIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
) {
    Circle(
        modifier = modifier,
        backgroundColor = PassTheme.colors.accentYellowOpaque,
        size = size,
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_note),
            contentDescription = stringResource(R.string.alias_title_icon_content_description),
            tint = PassTheme.colors.accentYellowOpaque
        )
    }
}

@Preview
@Composable
fun NoteIconPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            NoteIcon()
        }
    }
}
