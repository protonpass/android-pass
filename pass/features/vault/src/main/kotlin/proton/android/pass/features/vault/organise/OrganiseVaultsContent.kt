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

package proton.android.pass.features.vault.organise

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganiseVaultsContent(modifier: Modifier = Modifier, state: OrganiseVaultsUIState) {
    Scaffold(
        modifier = modifier,
        topBar = {
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            stickyHeader {
                Text("Visible vaults")
            }
            items(state.visibleVaults.size) { index ->
                Text(state.visibleVaults[index].name)
            }
            stickyHeader {
                Column {
                    Text("Hidden vaults")
                    Text(
                        "These vaults will not be accessible and their" +
                            " content won’t be available to Search or Autofill."
                    )
                }
            }
            items(state.hiddenVaults.size) { index ->
                Text(state.hiddenVaults[index].name)
            }
        }
    }
}
