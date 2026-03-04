/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.autofill.autofillhealth.ui

import proton.android.pass.autofill.autofillhealth.ui.composables.EventsTab
import proton.android.pass.autofill.autofillhealth.ui.composables.LogcatTab
import proton.android.pass.autofill.autofillhealth.viewmodel.AutofillHealthDebugUiState
import proton.android.pass.autofill.autofillhealth.viewmodel.AutofillHealthDebugViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme

@Composable
fun AutofillHealthDebugScreen(
    modifier: Modifier = Modifier,
    viewModel: AutofillHealthDebugViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissions()
    }
    AutofillHealthDebugContent(
        modifier = modifier,
        state = state,
        onClearLog = viewModel::clearLog,
        onShareEvents = { viewModel.shareEvents(context) },
        onToggleOverlay = viewModel::toggleOverlay,
        onClearLogcat = viewModel::clearLogcat,
        onRefreshLogcatState = viewModel::refreshLogcatState,
        onShareLogcat = { viewModel.shareLogcat(context) }
    )
}

private enum class DebugTab(val title: String) {
    Events("Events"),
    Logcat("Logcat")
}

@Composable
fun AutofillHealthDebugContent(
    modifier: Modifier = Modifier,
    state: AutofillHealthDebugUiState,
    initialTab: Int = 0,
    onClearLog: () -> Unit,
    onShareEvents: () -> Unit = {},
    onToggleOverlay: () -> Unit = {},
    onClearLogcat: () -> Unit = {},
    onRefreshLogcatState: () -> Unit = {},
    onShareLogcat: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.primary
        ) {
            DebugTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) }
                )
            }
        }

        when (DebugTab.entries[selectedTab]) {
            DebugTab.Events -> EventsTab(
                state = state,
                onClearLog = onClearLog,
                onShareEvents = onShareEvents,
                onToggleOverlay = onToggleOverlay
            )

            DebugTab.Logcat -> {
                LaunchedEffect(Unit) { onRefreshLogcatState() }
                LogcatTab(
                    entries = state.logcatEntries,
                    hasReadLogsPermission = state.hasReadLogsPermission,
                    isVerbosePropsEnabled = state.isVerbosePropsEnabled,
                    onClearLogcat = onClearLogcat,
                    onShareLogcat = onShareLogcat
                )
            }
        }
    }
}

@Preview
@Composable
fun HealthDebugPreview(
    @PreviewParameter(HealthDebugPreviewProvider::class)
    input: Pair<Boolean, HealthDebugPreviewData>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AutofillHealthDebugContent(
                state = input.second.state,
                initialTab = input.second.initialTab,
                onClearLog = {}
            )
        }
    }
}
