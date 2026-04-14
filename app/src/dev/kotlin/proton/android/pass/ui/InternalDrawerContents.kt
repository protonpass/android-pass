/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun InternalDrawerContents(
    modifier: Modifier = Modifier,
    onOpenFeatureFlag: () -> Unit,
    onAppNavigation: (AppNavigation) -> Unit,
    viewModel: InternalDrawerViewModel = hiltViewModel()
) {
    var selectedType by remember { mutableStateOf(RemoteInAppMessageType.Banner) }
    var quantity by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item { ShowkaseDrawerButton() }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.clearPreferences() }
            ) { Text(text = "Clear preferences") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    try {
                        throw DeveloperException("This is a test.")
                    } catch (e: DeveloperException) {
                        PassLogger.e("Internal", e)
                    }
                }
            ) { Text(text = "Trigger Crash") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.clearIconCache() }
            ) { Text(text = "Clear icon cache") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenFeatureFlag
            ) { Text(text = "Feature flags preferences") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.runSecurityChecks() }
            ) { Text(text = "Run security check") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.setAccessKey() }
            ) { Text(text = "Set access key") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.performSrp() }
            ) { Text(text = "Perform SRP") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.removeAccessKey() }
            ) { Text(text = "Remove access key") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.clearAttachments() }
            ) { Text(text = "Clear attachments") }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { viewModel.displayAllFeatureDiscoveryBanners() }
            ) { Text(text = "Force display feature discovery banners") }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Remote IAM Generator",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RemoteInAppMessageType.entries.forEach { type ->
                        val selected = type == selectedType
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = type },
                            colors = if (selected) ButtonDefaults.buttonColors()
                            else ButtonDefaults.outlinedButtonColors()
                        ) { Text(type.label) }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { if (quantity > 1) quantity-- }) { Text("-") }
                    Text(
                        text = "$quantity",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { if (quantity < 20) quantity++ }) { Text("+") }
                    Button(
                        modifier = Modifier.weight(2f),
                        onClick = { viewModel.insertMessages(selectedType, quantity) }
                    ) { Text("Add IAM") }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.clearIAMLastSeen() }
                ) { Text("Clear IAM cooldown") }
            }
        }
    }
}

class DeveloperException(message: String) : Exception(message)
