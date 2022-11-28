package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemRow
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemSubtitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.item.icon.AliasIcon

@ExperimentalMaterialApi
@Composable
fun AliasOptionsBottomSheetContents(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(textId = R.string.bottomsheet_copy_username) },
            subtitle = { BottomSheetItemSubtitle(textId = R.string.bottomsheet_move_to_trash) },
            icon = { AliasIcon() }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = listOf(
                copyUsername(),
                copyPassword(),
                edit(),
                moveToTrash()
            )
        )
    }
}

private fun copyUsername(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(textId = R.string.bottomsheet_copy_username) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val onClick: () -> Unit
        get() = { }
}

private fun copyPassword(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(textId = R.string.bottomsheet_copy_password) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val onClick: () -> Unit
        get() = { }
}

private fun edit(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemTitle(textId = R.string.bottomsheet_edit) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_pencil) }
    override val onClick: () -> Unit
        get() = { }
}

private fun moveToTrash(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            BottomSheetItemTitle(
                textId = R.string.bottomsheet_move_to_trash,
                textcolor = ProtonTheme.colors.notificationError
            )
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = {
            BottomSheetItemIcon(
                iconId = me.proton.core.presentation.R.drawable.ic_proton_trash,
                tint = ProtonTheme.colors.notificationError
            )
        }
    override val onClick: () -> Unit
        get() = { }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun AliasOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AliasOptionsBottomSheetContents()
        }
    }
}

