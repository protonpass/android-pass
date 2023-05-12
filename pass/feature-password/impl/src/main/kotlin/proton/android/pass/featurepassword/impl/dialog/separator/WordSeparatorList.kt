package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.PersistentList
import proton.android.pass.common.api.Option
import proton.android.pass.featurepassword.impl.dialog.DialogOptionRow
import proton.android.pass.featurepassword.impl.extensions.toResourceString
import proton.android.pass.password.api.PasswordGenerator

@Composable
fun WordSeparatorList(
    modifier: Modifier = Modifier,
    options: PersistentList<PasswordGenerator.WordSeparator>,
    selected: Option<PasswordGenerator.WordSeparator>,
    onSelected: (PasswordGenerator.WordSeparator) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(items = options, key = { it.name }) { option ->
            DialogOptionRow(
                value = option.toResourceString(),
                isSelected = selected.value() == option,
                onClick = { onSelected(option) }
            )
        }
    }
}
