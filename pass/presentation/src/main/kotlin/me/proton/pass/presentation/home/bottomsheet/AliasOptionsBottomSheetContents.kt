package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemText
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@ExperimentalMaterialApi
@Composable
fun AliasOptionsBottomSheetContents(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        BottomSheetTitle(title = R.string.sorting_bottomsheet_title)
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
        get() = { BottomSheetItemText(textId = R.string.bottomsheet_copy_username) }
    override val subtitle: (() -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val onClick: () -> Unit
        get() = { }
}

private fun copyPassword(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.bottomsheet_copy_password) }
    override val subtitle: (() -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
    override val onClick: () -> Unit
        get() = { }
}

private fun edit(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.bottomsheet_edit) }
    override val subtitle: (() -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = R.drawable.ic_pencil) }
    override val onClick: () -> Unit
        get() = { }
}

private fun moveToTrash(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            Text(
                text = stringResource(id = R.string.bottomsheet_move_to_trash),
                style = ProtonTheme.typography.default,
                color = ProtonTheme.colors.notificationError
            )
        }
    override val subtitle: (() -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_trash),
                contentDescription = stringResource(id = R.string.bottomsheet_content_description_item_icon),
                tint = ProtonTheme.colors.notificationError
            )
        }
    override val onClick: () -> Unit
        get() = { }
}
