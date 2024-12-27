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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton

@Composable
fun AliasAdvancedOptions(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    prefix: String,
    suffix: AliasSuffixUiModel?,
    isError: Boolean,
    canSelectSuffix: Boolean,
    showAdvancedOptionsInitially: Boolean = false,
    onPrefixChanged: (String) -> Unit,
    onSuffixClicked: () -> Unit
) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(showAdvancedOptionsInitially) }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            ShowAdvancedOptionsButton(
                currentValue = showAdvancedOptions,
                onClick = { showAdvancedOptions = !showAdvancedOptions }
            )
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            AliasAdvancedOptionsSection(
                enabled = enabled,
                isBottomSheet = false,
                prefix = prefix,
                suffix = suffix,
                isError = isError,
                canSelectSuffix = canSelectSuffix,
                onPrefixChanged = onPrefixChanged,
                onSuffixClicked = onSuffixClicked
            )
        }
    }
}
