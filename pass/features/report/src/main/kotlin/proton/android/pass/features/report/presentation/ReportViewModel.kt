/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.report.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.entity.validate
import me.proton.core.report.domain.usecase.SendBugReport
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val autofillManager: AutofillManager,
    private val sendBugReport: SendBugReport
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    private var reportFormData by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ReportFormData()) }

    fun openAutofillSettings() {
        autofillManager.openAutofillSelector()
    }

    fun trySendingBugReport() {
        viewModelScope.launch {
            val snapshotData = reportFormData
            val bugReport = BugReport(
                title = snapshotData.subject,
                description = snapshotData.description,
                email = snapshotData.email,
                username = snapshotData.username,
                shouldAttachLog = snapshotData.attachLog
            )
            val formErrors: List<BugReportValidationError> = bugReport.validate()

            if (formErrors.isEmpty()) {
                sendBugReport(bugReport)
            } else {
                PassLogger.i(TAG, "Form errors: $formErrors")
            }
        }
    }

    companion object {
        private const val TAG = "ReportViewModel"
    }
}
