package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.pass.domain.CustomFieldContent

@Composable
fun CustomFieldEntry(
    modifier: Modifier = Modifier,
    entry: CustomFieldContent,
    canEdit: Boolean,
    onValueChange: (String) -> Unit
) {
    when (entry) {
        is CustomFieldContent.Text -> TextCustomFieldEntry(
            modifier = modifier,
            content = entry,
            canEdit = canEdit,
            onChange = onValueChange
        )
        is CustomFieldContent.Hidden -> HiddenCustomFieldEntry(
            modifier = modifier,
            content = entry,
            canEdit = canEdit,
            onChange = onValueChange
        )
        is CustomFieldContent.Totp -> TotpCustomFieldEntry(
            modifier = modifier,
            content = entry,
            canEdit = canEdit,
            onChange = onValueChange
        )
    }
}
