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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import proton.android.pass.autofill.autofillhealth.model.LogcatEntry
import proton.android.pass.autofill.autofillhealth.viewmodel.AutofillHealthDebugUiState
import proton.android.pass.commonui.api.ThemePairPreviewProvider

data class HealthDebugPreviewData(
    val initialTab: Int,
    val state: AutofillHealthDebugUiState
)

private class HealthDebugDataProvider : PreviewParameterProvider<HealthDebugPreviewData> {

    override val values: Sequence<HealthDebugPreviewData> = sequenceOf(
        HealthDebugPreviewData(
            initialTab = 0,
            state = AutofillHealthDebugUiState(
                isConnected = true,
                currentIme = "GBoard",
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
                ),
                hasOverlayPermissionInManifest = true,
                canShowOverlay = true
            )
        ),
        HealthDebugPreviewData(
            initialTab = 1,
            state = AutofillHealthDebugUiState(
                hasReadLogsPermission = false,
                isVerbosePropsEnabled = false,
                logcatEntries = previewLogcatEntries()
            )
        ),
        HealthDebugPreviewData(
            initialTab = 1,
            state = AutofillHealthDebugUiState(
                hasReadLogsPermission = true,
                isVerbosePropsEnabled = true,
                logcatEntries = previewLogcatEntries()
            )
        )
    )
}

class HealthDebugPreviewProvider : ThemePairPreviewProvider<HealthDebugPreviewData>(
    HealthDebugDataProvider()
)

private fun previewLogcatEntries() = listOf(
    LogcatEntry(
        timestamp = "03-04 10:15:32.123",
        level = 'D',
        tag = "AutofillManager",
        message = "Fill request received",
        isOwnProcess = true
    ),
    LogcatEntry(
        timestamp = "03-04 10:15:32.456",
        level = 'I',
        tag = "AutofillService",
        message = "Processing autofill",
        isOwnProcess = true
    ),
    LogcatEntry(
        timestamp = "03-04 10:15:33.789",
        level = 'W',
        tag = "Autofill",
        message = "No datasets found",
        isOwnProcess = false
    ),
    LogcatEntry(
        timestamp = "03-04 10:15:34.012",
        level = 'E',
        tag = "AutofillManager",
        message = "Error filling view",
        isOwnProcess = false
    )
)
