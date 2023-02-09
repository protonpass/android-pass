package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer

@Composable
fun LinkedAppsList(
    modifier: Modifier = Modifier,
    list: ImmutableSet<String>,
    isEditable: Boolean,
    onLinkedAppDelete: (String) -> Unit
) {
    if (list.isNotEmpty()) {
        RoundedCornersContainer(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                list.forEach { packageName ->
                    LinkedAppItem(
                        packageName = packageName,
                        isEditable = isEditable,
                        onLinkedAppDelete = onLinkedAppDelete
                    )
                }
            }
        }
    }
}
