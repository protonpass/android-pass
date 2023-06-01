package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent

@Composable
fun CustomFieldEntry(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent,
    onToggleVisibility: () -> Unit,
    onCopyValue: () -> Unit,
    onCopyValueWithContent: (String) -> Unit,
    onUpgradeClick: () -> Unit
) {
    when (entry) {
        is CustomFieldUiContent.Text -> CustomFieldText(
            modifier = modifier,
            entry = entry,
            onCopyValue = onCopyValueWithContent
        )
        is CustomFieldUiContent.Hidden -> CustomFieldHidden(
            modifier = modifier,
            entry = entry,
            onToggleVisibility = onToggleVisibility,
            onCopyValue = onCopyValue
        )
        is CustomFieldUiContent.Totp -> CustomFieldTotp(
            modifier = modifier,
            entry = entry,
            onCopyTotpClick = onCopyValueWithContent,
            onUpgradeClick = onUpgradeClick
        )
    }
}
