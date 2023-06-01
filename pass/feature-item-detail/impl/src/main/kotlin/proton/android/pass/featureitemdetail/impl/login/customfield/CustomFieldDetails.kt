package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent

@Composable
fun CustomFieldDetails(
    modifier: Modifier = Modifier,
    fields: List<CustomFieldUiContent>,
    onEvent: (CustomFieldEvent) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        fields.forEachIndexed { idx, entry ->
            CustomFieldEntry(
                entry = entry,
                onToggleVisibility = {
                    onEvent(CustomFieldEvent.ToggleFieldVisibility(idx))
                },
                onCopyValue = {
                    onEvent(CustomFieldEvent.CopyValue(idx))
                },
                onCopyValueWithContent = { onEvent(CustomFieldEvent.CopyValueContent(it)) }
            )
        }
    }
}
