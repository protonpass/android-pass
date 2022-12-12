package me.proton.pass.presentation.trash

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@ExperimentalMaterialApi
@Composable
fun TrashAllBottomSheetContents(
    modifier: Modifier = Modifier,
    onEmptyTrash: () -> Unit,
    onRestoreAll: () -> Unit
) {
    Column(modifier) {
        BottomSheetTitle(title = R.string.bottomsheet_trash_all_items_title, showDivider = false)
        BottomSheetItemList(
            items = listOf(
                restoreAll(onRestoreAll),
                emptyTrash(onEmptyTrash)
            )
        )
    }
}

private fun restoreAll(
    onRestoreAll: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_restore_all_items)) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_clock_rotate_left) }
    override val onClick: () -> Unit
        get() = onRestoreAll
}

private fun emptyTrash(
    onEmptyTrash: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                text = stringResource(id = R.string.bottomsheet_empty_trash),
                textcolor = ProtonTheme.colors.notificationError
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = me.proton.core.presentation.R.drawable.ic_proton_trash_cross,
                tint = ProtonTheme.colors.notificationError
            )
        }
    override val onClick: () -> Unit
        get() = onEmptyTrash
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun TrashAllBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            TrashAllBottomSheetContents(
                onEmptyTrash = {},
                onRestoreAll = {}
            )
        }
    }
}

