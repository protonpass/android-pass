package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.featurecreateitem.impl.R

@Composable
fun AddTotpBottomSheet(
    modifier: Modifier = Modifier,
    onAddTotp: (AddTotpType) -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(
            title = stringResource(R.string.totp_bottom_sheet_title),
            showDivider = false
        )
        BottomSheetItemList(
            items = AddTotpType.values()
                .map { createTotpBottomSheetItem(it, onAddTotp) }
                .toPersistentList()
        )
    }
}

private fun createTotpBottomSheetItem(
    totpType: AddTotpType,
    onTotpClick: (AddTotpType) -> Unit
): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = totpType.textId)) }
        override val subtitle: @Composable (() -> Unit)? = null
        override val icon: @Composable (() -> Unit)? = null
        override val onClick: () -> Unit = { onTotpClick(totpType) }
    }
