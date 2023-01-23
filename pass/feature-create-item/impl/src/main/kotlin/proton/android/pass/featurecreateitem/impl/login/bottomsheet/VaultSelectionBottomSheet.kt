package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.featurecreateitem.impl.R
import proton.pass.domain.ShareId

@Composable
fun VaultSelectionBottomSheet(
    modifier: Modifier = Modifier,
    shareList: List<ShareUiModel>,
    selectedShare: ShareUiModel,
    onVaultClick: (ShareId) -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(
            title = stringResource(R.string.vault_title),
            showDivider = false
        )
        BottomSheetItemList(
            items = shareList
                .map {
                    createVaultBottomSheetItem(
                        shareUiModel = it,
                        isSelected = it.id == selectedShare.id,
                        onVaultClick = onVaultClick
                    )
                }
                .toPersistentList()
        )
    }
}

private fun createVaultBottomSheetItem(
    shareUiModel: ShareUiModel,
    isSelected: Boolean,
    onVaultClick: (ShareId) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = shareUiModel.name) }
        override val subtitle: @Composable (() -> Unit)?
            get() = null
        override val icon: @Composable (() -> Unit)?
            get() = if (isSelected) {
                { BottomSheetItemIcon(iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark) }
            } else null
        override val onClick: () -> Unit = { onVaultClick(shareUiModel.id) }
    }
