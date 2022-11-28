package me.proton.pass.presentation.create.login.bottomsheet

import androidx.compose.foundation.layout.Column
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
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemText
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@Composable
fun AliasOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onEditAliasClick: () -> Unit,
    onRemoveAliasClick: () -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(
            title = me.proton.pass.domain.R.string.item_type_alias,
            showDivider = false
        )
        BottomSheetItemList(
            items = listOf(
                createEdit(onEditAliasClick),
                createRemoveAlias(onRemoveAliasClick),
            )
        )
    }
}

private fun createEdit(onEditAlias: () -> Unit): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = { BottomSheetItemText(textId = R.string.action_edit) }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val icon: (@Composable () -> Unit)
        get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_pencil) }
    override val onClick: () -> Unit
        get() = onEditAlias
}


private fun createRemoveAlias(onRemoveAlias: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemText(textId = R.string.action_remove) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = me.proton.core.presentation.R.drawable.ic_proton_trash
                )
            }
        override val onClick: () -> Unit
            get() = onRemoveAlias
    }

@Preview
@Composable
fun AliasOptionsBottomSheetPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AliasOptionsBottomSheet(
                onEditAliasClick = {},
                onRemoveAliasClick = {}
            )
        }
    }
}
