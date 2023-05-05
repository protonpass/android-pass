package proton.android.pass.featurehome.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.featurehome.impl.R

@ExperimentalMaterialApi
@Composable
fun VaultOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    showMigrate: Boolean,
    showDelete: Boolean,
    onEdit: () -> Unit,
    onMigrate: () -> Unit,
    onRemove: () -> Unit
) {
    val items = mutableListOf(editVault(onEdit))
    if (showMigrate) {
        items.add(migrateVault(onMigrate))
    }
    if (showDelete) {
        items.add(removeVault(onRemove))
    }

    Column(modifier.bottomSheet()) {
        BottomSheetItemList(
            items = if (items.size > 1) {
                items.withDividers()
            } else {
                items
            }.toPersistentList()
        )
    }
}

private fun editVault(onEdit: () -> Unit): BottomSheetItem =
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
            get() = { onEdit() }
        override val isDivider = false
    }

private fun migrateVault(onMigrate: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_migrate)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_folder_arrow_in) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onMigrate() }
        override val isDivider = false
    }

private fun removeVault(onRemove: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_delete_vault),
                    color = ProtonTheme.colors.notificationError
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = me.proton.core.presentation.R.drawable.ic_proton_trash_cross,
                    tint = ProtonTheme.colors.notificationError
                )
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onRemove() }
        override val isDivider = false
    }

class ThemeVaultOptionsInput :
    ThemePairPreviewProvider<VaultOptionsInput>(VaultOptionsBottomSheetContentsPreviewProvider())

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun VaultOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemeVaultOptionsInput::class) input: Pair<Boolean, VaultOptionsInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            VaultOptionsBottomSheetContents(
                showDelete = input.second.showDelete,
                showMigrate = input.second.showMigrate,
                onEdit = {},
                onMigrate = {},
                onRemove = {}
            )
        }
    }
}
