package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.R

@Composable
fun LinkedAppsListSection(
    modifier: Modifier = Modifier,
    packageInfoUiSet: ImmutableSet<PackageInfoUi>,
    isEditable: Boolean,
    onLinkedAppDelete: (PackageInfoUi) -> Unit
) {
    if (packageInfoUiSet.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp, 12.dp, 0.dp, 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionTitle(text = stringResource(R.string.linked_apps_title))
        packageInfoUiSet.forEach { packageInfoUi ->
            LinkedAppItem(
                packageInfoUi = packageInfoUi,
                isEditable = isEditable,
                onLinkedAppDelete = onLinkedAppDelete
            )
        }
    }
}
