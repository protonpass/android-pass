package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent
import proton.android.pass.featureitemdetail.impl.login.TotpUiState
import proton.android.pass.featureitemdetail.impl.login.totp.TotpRowContent

@Composable
fun CustomFieldTotp(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Totp,
    onCopyTotpClick: (String) -> Unit
) {
    RoundedCornersColumn(modifier = modifier.fillMaxWidth()) {
        TotpRowContent(
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
