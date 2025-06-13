/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.LinkedAppItem
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.item.details.modifiers.contentDiff
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs

@Composable
internal fun PassLoginItemDetailLinkedAppsSection(
    modifier: Modifier = Modifier,
    packageInfoUiSet: ImmutableSet<PackageInfoUi>,
    isEditable: Boolean,
    onLinkedAppDelete: (PackageInfoUi) -> Unit,
    itemDiffs: ItemDiffs.Login
) {

    val (linkedAppsSectionItemDiffType, linkedAppsFieldsItemDiffTypes) =
        remember(itemDiffs) { itemDiffs.linkedApps }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .contentDiff(itemDiffType = linkedAppsSectionItemDiffType)
            .applyIf(
                condition = linkedAppsSectionItemDiffType == ItemDiffType.Field,
                ifTrue = {
                    padding(
                        horizontal = Spacing.medium,
                        vertical = Spacing.small
                    )
                }
            ),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        SectionTitle(text = stringResource(R.string.linked_apps_title))

        packageInfoUiSet.forEachIndexed { index, packageInfoUi ->
            LinkedAppItem(
                packageInfoUi = packageInfoUi,
                isEditable = isEditable,
                onLinkedAppDelete = onLinkedAppDelete,
                itemDiffType = linkedAppsFieldsItemDiffTypes.getOrElse(index) { ItemDiffType.None }
            )
        }
    }
}
