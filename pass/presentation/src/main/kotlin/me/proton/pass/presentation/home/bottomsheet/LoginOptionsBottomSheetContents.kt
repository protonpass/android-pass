package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemIcon
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemList
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemRow
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemSubtitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItemTitle
import me.proton.pass.presentation.components.common.item.icon.LoginIcon
import me.proton.pass.presentation.components.model.ItemUiModel

@ExperimentalMaterialApi
@Composable
fun LoginOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    onCopyUsername: (String) -> Unit,
    onCopyPassword: (String) -> Unit,
    onEdit: (ShareId, ItemId) -> Unit,
    onMoveToTrash: (ItemUiModel) -> Unit
) {
    val itemType = itemUiModel.itemType as ItemType.Login
    Column(modifier) {
        BottomSheetItemRow(
            title = { BottomSheetItemTitle(text = itemUiModel.name) },
            subtitle = {
                BottomSheetItemSubtitle(
                    text = itemType.username
                )
            },
            icon = { LoginIcon() }
        )
        Divider(modifier = Modifier.fillMaxWidth())
        BottomSheetItemList(
            items = listOf(
                copyUsername(itemType.username, onCopyUsername),
                copyPassword(itemType.password, onCopyPassword),
                edit(itemUiModel, onEdit),
                moveToTrash(itemUiModel, onMoveToTrash)
            )
        )
    }
}

private fun copyUsername(username: String, onCopyUsername: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_username)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val onClick: () -> Unit
            get() = { onCopyUsername(username) }
    }

private fun copyPassword(password: String, onCopyPassword: (String) -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_copy_password)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val icon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = R.drawable.ic_squares) }
        override val onClick: () -> Unit
            get() = { onCopyPassword(password) }
    }

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun LoginOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            LoginOptionsBottomSheetContents(
                Modifier,
                itemUiModel = ItemUiModel(
                    id = ItemId(id = ""),
                    shareId = ShareId(id = ""),
                    name = "My Login",
                    itemType = ItemType.Login("My username", "My password", emptyList())
                ),
                {}, {}, { _, _ -> }, {}
            )
        }
    }
}

