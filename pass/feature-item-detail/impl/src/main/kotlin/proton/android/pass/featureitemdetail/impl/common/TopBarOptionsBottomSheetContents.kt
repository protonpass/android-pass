package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle

@Composable
fun TopBarOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    onMoveToTrash: () -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = persistentListOf(
            moveToTrash(onClick = { onMoveToTrash() })
        )
    )
}

private fun moveToTrash(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = "Move to trash") }
        override val subtitle: @Composable (() -> Unit)?
            get() = null
        override val icon: @Composable (() -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_trash) }
        override val onClick: () -> Unit
            get() = { onClick() }
    }

@Preview
@Composable
fun TopBarOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            TopBarOptionsBottomSheetContents(
                onMoveToTrash = {}
            )
        }
    }
}
