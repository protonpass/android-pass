package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent
import proton.android.pass.featureitemdetail.impl.login.TotpUiState
import proton.android.pass.featureitemdetail.impl.login.totp.TotpRowContent
import proton.android.pass.featureitemdetail.impl.login.totp.TotpUpgradeContent

@Composable
fun CustomFieldTotp(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Totp,
    onCopyTotpClick: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        when (entry) {
            is CustomFieldUiContent.Totp.Limited -> TotpUpgradeContent(
                modifier = modifier,
                label = entry.label,
                onUpgrade = onUpgradeClick
            )

            is CustomFieldUiContent.Totp.Visible -> TotpRowContent(
                label = entry.label,
                state = TotpUiState.Visible(
                    code = entry.code,
                    remainingSeconds = entry.remainingSeconds,
                    totalSeconds = entry.totalSeconds
                ),
                onCopyTotpClick = onCopyTotpClick
            )
        }
    }
}
