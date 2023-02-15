package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.composecomponents.impl.form.LinkedAppItem
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.SectionTitle

@Composable
fun LinkedAppsListSection(
    modifier: Modifier = Modifier,
    linkedAppsSet: ImmutableSet<String>,
    isEditable: Boolean,
    onLinkedAppDelete: (String) -> Unit
) {
    if (linkedAppsSet.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle(text = stringResource(R.string.linked_apps_title))
        linkedAppsSet.forEach { packageName ->
            LinkedAppItem(
                packageName = packageName,
                isEditable = isEditable,
                onLinkedAppDelete = onLinkedAppDelete
            )
        }
    }
}
