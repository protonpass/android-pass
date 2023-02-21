package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.PassDimens.bottomSheetPadding
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
    Column(modifier = modifier.bottomSheetPadding()) {
        BottomSheetTitle(
            title = stringResource(R.string.totp_bottom_sheet_title),
            showDivider = false
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        override val icon: @Composable (() -> Unit)
            get() = {
                Icon(
                    painter = painterResource(id = totpType.iconId),
                    contentDescription = stringResource(id = totpType.textId),
                )
            }
        override val onClick: () -> Unit = { onTotpClick(totpType) }
        override val isDivider = false
    }

@Preview
@Composable
fun AddTotpBottomSheetPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            AddTotpBottomSheet(onAddTotp = {})
        }
    }
}
