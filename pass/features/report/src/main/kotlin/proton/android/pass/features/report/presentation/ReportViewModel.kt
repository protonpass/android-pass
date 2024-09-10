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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.entity.validate
import me.proton.core.report.domain.usecase.SendBugReport
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.report.ui.ReportReason
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    private val autofillManager: AutofillManager,
    private val sendBugReport: SendBugReport
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    var formState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ReportFormData()) }

    private val reportReasonFlow: MutableStateFlow<Option<ReportReason>> = MutableStateFlow(None)
    private val isLoadingStateFlow = MutableStateFlow(IsLoadingState.NotLoading)
    private val formValidationErrorsStateFlow =
        MutableStateFlow(persistentListOf<ReportValidationError>())

    internal val state = combine(
        reportReasonFlow,
        isLoadingStateFlow,
        formValidationErrorsStateFlow,
        ::ReportState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportState.Initial
    )

    fun openAutofillSettings() {
        autofillManager.openAutofillSelector()
    }

    fun trySendingBugReport() {
        viewModelScope.launch {
            val bugReport = BugReport(
                title = formState.title,
                description = formState.description,
                email = formState.email,
                username = formState.username,
                shouldAttachLog = formState.attachLog
            )
            val formErrors: List<BugReportValidationError> = bugReport.validate()

            if (formErrors.isEmpty()) {
                sendBugReport(bugReport)
            } else {
                PassLogger.i(TAG, "Form errors: $formErrors")
            }
        }
    }

    fun onDescriptionChange(value: String) {
        formValidationErrorsStateFlow.update { list -> list.removeAll { it is DescriptionError } }
        formState = formState.copy(
            description = value
        )
    }

    fun onEmailChange(value: String) {
        formValidationErrorsStateFlow.update { list -> list.removeAll { it is EmailError } }
        formState = formState.copy(
            email = value
        )
    }

    fun onSendLogsChange(value: Boolean) {
        formState = formState.copy(
            attachLog = value
        )
    }

    fun onReasonChange(value: ReportReason) {
        reportReasonFlow.update { value.some() }
    }

    companion object {
        private const val TAG = "ReportViewModel"
    }
}


