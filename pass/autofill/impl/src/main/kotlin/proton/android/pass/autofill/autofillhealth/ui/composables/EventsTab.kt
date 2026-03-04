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

package proton.android.pass.autofill.autofillhealth.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.presentation.R as CoreR
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import proton.android.pass.autofill.autofillhealth.viewmodel.AutofillHealthDebugUiState
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle

@Composable
internal fun EventsTab(
    state: AutofillHealthDebugUiState,
    onClearLog: () -> Unit,
    onShareEvents: () -> Unit,
    onToggleOverlay: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.small))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusCard(isConnected = state.isConnected)
                Spacer(modifier = Modifier.width(Spacing.small))
                InfoCard(
                    lastFillRequest = state.lastFillRequest,
                    currentIme = state.currentIme
                )
            }
        }

        if (state.hasOverlayPermissionInManifest) {
            item {
                OverlayCard(
                    canShowOverlay = state.canShowOverlay,
                    isOverlayVisible = state.isOverlayVisible,
                    onToggle = onToggleOverlay
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Event Log (${state.events.size})",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Circle(
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onShareEvents
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_arrow_up_from_square),
                            contentDescription = "Share",
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                    Circle(
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onClearLog
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_trash),
                            contentDescription = "Clear",
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                }
            }
        }
        items(state.events) { event ->
            EventRow(event = event)
            Divider()
        }
        item {
            Spacer(modifier = Modifier.height(Spacing.medium))
        }
    }
}

@Preview
@Composable
fun EventsTabPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            EventsTab(
                state = AutofillHealthDebugUiState(
                    isConnected = true,
                    currentIme = "Gboard",
                    lastFillRequest = AutofillHealthEvent(
                        timestamp = 1_700_000_000_000L,
                        type = AutofillHealthEventType.FILL_REQUEST_INLINE,
                        packageName = "com.example.app"
                    ),
                    events = listOf(
                        AutofillHealthEvent(
                            timestamp = 1_700_000_000_000L,
                            type = AutofillHealthEventType.FILL_REQUEST_INLINE,
                            packageName = "com.example.app"
                        ),
                        AutofillHealthEvent(
                            timestamp = 1_700_000_000_000L,
                            type = AutofillHealthEventType.CONNECTED
                        )
                    )
                ),
                onClearLog = {},
                onShareEvents = {},
                onToggleOverlay = {}
            )
        }
    }
}
