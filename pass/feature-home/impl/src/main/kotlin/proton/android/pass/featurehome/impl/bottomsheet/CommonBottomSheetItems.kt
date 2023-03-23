package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.featurehome.impl.R
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

internal fun edit(itemUiModel: ItemUiModel, onEdit: (ShareId, ItemId) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_edit)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_pencil) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onEdit(itemUiModel.shareId, itemUiModel.id) }
        override val isDivider = false
    }

internal fun moveToTrash(
    itemUiModel: ItemUiModel,
    onMoveToTrash: (ItemUiModel) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_move_to_trash)
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = me.proton.core.presentation.R.drawable.ic_proton_trash
                )
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onMoveToTrash(itemUiModel) }
        override val isDivider = false
    }
