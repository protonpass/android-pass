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

package proton.android.pass.features.itemcreate.alias.suffixes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.itemcreate.alias.AliasSuffixUiModel

@Suppress("UnusedPrivateMember")
@Composable
fun SelectSuffixDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    canUpgrade: Boolean = false,
    suffixes: ImmutableList<AliasSuffixUiModel>,
    selectedSuffix: AliasSuffixUiModel?,
    color: Color,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    if (!show) return

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        SelectSuffixContent(
            suffixes = suffixes,
            canUpgrade = canUpgrade,
            selectedSuffix = selectedSuffix,
            color = color,
            onSuffixChanged = onSuffixChanged,
            onDismiss = onDismiss,
            onUpgrade = onUpgrade
        )
    }
}
