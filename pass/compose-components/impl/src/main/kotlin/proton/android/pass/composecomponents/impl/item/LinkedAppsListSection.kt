/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
