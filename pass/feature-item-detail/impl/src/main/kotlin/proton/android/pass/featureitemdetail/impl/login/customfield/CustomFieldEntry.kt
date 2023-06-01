package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.pass.domain.CustomFieldContent

@Composable
fun CustomFieldEntry(
    modifier: Modifier = Modifier,
    entry: CustomFieldContent,
    onToggleVisibility: () -> Unit,
    onCopyValue: () -> Unit
) {
    when (entry) {
        is CustomFieldContent.Text -> CustomFieldText(
            modifier = modifier,
            entry = entry,
            onCopyValue = onCopyValue
        )
        is CustomFieldContent.Hidden -> CustomFieldHidden(
            modifier = modifier,
            entry = entry,
            onToggleVisibility = onToggleVisibility,
            onCopyValue = onCopyValue
        )
        is CustomFieldContent.Totp -> CustomFieldTotp(
            modifier = modifier,
        )
    }
}
