package proton.android.pass.composecomponents.impl.item.icon

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.BoxedIcon

@Composable
fun NoteIcon(
    modifier: Modifier = Modifier,
    size: Int = 40,
    shape: Shape = PassTheme.shapes.squircleMediumShape
) {
    BoxedIcon(
        modifier = modifier,
        backgroundColor = PassTheme.colors.noteInteractionNormMajor1,
        size = size,
        shape = shape
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_file_lines),
            contentDescription = stringResource(R.string.note_title_icon_content_description),
            tint = PassTheme.colors.noteInteractionNormMajor1
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
            NoteIcon(shape = PassTheme.shapes.squircleMediumShape)
        }
    }
}
