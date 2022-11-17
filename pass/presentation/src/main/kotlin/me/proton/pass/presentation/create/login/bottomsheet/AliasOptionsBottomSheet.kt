package me.proton.pass.presentation.create.login.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetItem
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@Composable
fun AliasOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onEditAliasClick: () -> Unit,
    onRemoveAliasClick: () -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(title = me.proton.pass.domain.R.string.item_type_alias, showDivider = false)
        BottomSheetItem(
            modifier = Modifier
                .clickable(onClick = onEditAliasClick)
                .padding(vertical = 8.dp),
            icon = me.proton.core.presentation.R.drawable.ic_proton_pencil,
            title = R.string.action_edit
        )
        BottomSheetItem(
            modifier = Modifier
                .clickable(onClick = onRemoveAliasClick)
                .padding(vertical = 8.dp),
            icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
            title = R.string.action_remove,
            tint = ProtonTheme.colors.notificationError
        )
    }
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
