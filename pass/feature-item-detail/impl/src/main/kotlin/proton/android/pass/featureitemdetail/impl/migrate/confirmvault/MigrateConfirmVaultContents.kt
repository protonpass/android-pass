package proton.android.pass.featureitemdetail.impl.migrate.confirmvault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.featureitemdetail.impl.R

@Composable
fun MigrateConfirmVaultContents(
    modifier: Modifier = Modifier,
    state: MigrateConfirmVaultUiState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.migrate_item_confirm_title_bottom_sheet),
            textAlign = TextAlign.Center,
            color = PassTheme.colors.textNorm
        )

        if (state.vault is Some) {
            BottomSheetItemList(
                items = persistentListOf(
                    bottomSheetDivider(),
                    BottomSheetVaultRow(
                        vault = state.vault.value,
                        isSelected = false,
                        onVaultClick = null
                    ),
                    bottomSheetDivider()
                ),
            )
        }

        BottomSheetCancelConfirm(
            isLoading = state.isLoading.value(),
            confirmText = stringResource(R.string.migrate_item_confirm_confirm_button),
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}
