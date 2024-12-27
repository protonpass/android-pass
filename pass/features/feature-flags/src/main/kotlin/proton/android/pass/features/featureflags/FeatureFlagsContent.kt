/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.featureflags

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.preferences.FeatureFlag

@Composable
fun FeatureFlagsContent(
    modifier: Modifier = Modifier,
    state: Map<FeatureFlag, Any>,
    onToggle: (FeatureFlag, Boolean) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar { Text(text = "Feature Flags") } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            state.forEach { (featureFlag, value) ->
                when (value) {
                    is Boolean -> item {
                        BoolFeatureFlagItem(
                            featureFlag = featureFlag,
                            value = value,
                            onToggle = onToggle
                        )
                    }
                }
            }
        }
    }
}
