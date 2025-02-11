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

package proton.android.pass.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun InternalDrawerContents(
    modifier: Modifier = Modifier,
    onOpenFeatureFlag: () -> Unit,
    onAppNavigation: (AppNavigation) -> Unit,
    viewModel: InternalDrawerViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShowkaseDrawerButton()
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.clearPreferences() },
        ) {
            Text(text = "Clear preferences")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                try {
                    throw DeveloperException("This is a test.")
                } catch (e: DeveloperException) {
                    PassLogger.e("Internal", e)
                }
            },
        ) {
            Text(text = "Trigger Crash")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.clearIconCache() },
        ) {
            Text(text = "Clear icon cache")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenFeatureFlag,
        ) {
            Text(text = "Feature flags preferences")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.runSecurityChecks() },
        ) {
            Text(text = "Run security check")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.setAccessKey() },
        ) {
            Text(text = "Set access key")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.performSrp() },
        ) {
            Text(text = "Perform SRP")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.removeAccessKey() },
        ) {
            Text(text = "Remove access key")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.clearAttachments() },
        ) {
            Text(text = "Clear attachments")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.displayAllFeatureDiscoveryBanners() },
        ) {
            Text(text = "Force display feature discovery banners")
        }
    }
}

class DeveloperException(message: String) : Exception(message)
