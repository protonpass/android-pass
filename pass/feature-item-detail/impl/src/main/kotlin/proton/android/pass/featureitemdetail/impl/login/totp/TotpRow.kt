package proton.android.pass.featureitemdetail.impl.login.totp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemdetail.impl.login.TotpUiState

@Composable
fun TotpRow(
    modifier: Modifier = Modifier,
    state: TotpUiState,
    onCopyTotpClick: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    when (state) {
        TotpUiState.Hidden -> TotpUpgradeContent(
            modifier = modifier,
            onUpgrade = onUpgradeClick
        )
        is TotpUiState.Visible -> TotpRowContent(
            modifier = modifier,
            state = state,
            onCopyTotpClick = onCopyTotpClick
        )
    }
}
