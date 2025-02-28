/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.item.SectionTitle

@Composable
internal fun PassItemDetailsSection(
    modifier: Modifier = Modifier,
    title: String,
    sections: ImmutableList<@Composable () -> Unit>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        SectionTitle(text = title)

        PassItemDetailMainSectionContainer(sections = sections)
    }
}
