package proton.android.pass.featurehome.impl.trash

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.EmptyList
import proton.android.pass.featurehome.impl.R

@Composable
fun EmptyTrashContent(
    modifier: Modifier = Modifier
) {
    EmptyList(
        modifier = modifier,
        emptyListTitle = stringResource(R.string.trash_empty_list_title),
        emptyListMessage = stringResource(R.string.trash_empty_list_message),
        emptyListImage = R.drawable.empty_trash
    )
}

@Preview
@Composable
fun EmptyTrashContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            EmptyTrashContent()
        }
    }
}
