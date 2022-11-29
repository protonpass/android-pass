package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.model.ItemUiModel

internal fun edit(itemUiModel: ItemUiModel, onEdit: (ShareId, ItemId) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_edit)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_pencil) }
        override val onClick: () -> Unit
            get() = { onEdit(itemUiModel.shareId, itemUiModel.id) }
    }

internal fun moveToTrash(itemUiModel: ItemUiModel, onMoveToTrash: (ItemUiModel) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_move_to_trash),
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
            get() = { onMoveToTrash(itemUiModel) }
    }
