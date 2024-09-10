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
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.report.Report
import proton.android.pass.data.api.usecases.report.SendReport
import proton.android.pass.features.report.presentation.ReportFormData.Companion.validate
import proton.android.pass.features.report.ui.ReportReason
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandleProvider: SavedStateHandleProvider,
    accountManager: AccountManager,
    userManager: UserManager,
    private val autofillManager: AutofillManager,
    private val sendReport: SendReport
) : ViewModel() {

    init {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            val user = userId?.let { userManager.getUser(it) }
            val email = user?.email ?: "unknown"
            val username = user?.name ?: userId?.id ?: "unknown"
            formState = formState.copy(
                email = email,
                username = username
            )
        }
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    var formState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ReportFormData()) }

    private val reportReasonFlow: MutableStateFlow<Option<ReportReason>> = MutableStateFlow(None)
    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(IsLoadingState.NotLoading)
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
            val errors: List<ReportValidationError> = formState.validate()
            formValidationErrorsStateFlow.update { errors.toPersistentList() }
            if (errors.isEmpty()) {
                val report = Report(
                    title = "Report from Pass Android: ${reportReasonFlow.value}",
                    description = formState.description,
                    email = formState.email,
                    username = formState.username,
                    shouldAttachLog = formState.attachLog
                )
                isLoadingStateFlow.update { IsLoadingState.Loading }
                runCatching { sendReport(report) }
                    .onSuccess {
                        PassLogger.i(TAG, "Report sent successfully")
                    }
                    .onError {
                        PassLogger.w(TAG, "Error sending report")
                        PassLogger.w(TAG, it)
                    }
                isLoadingStateFlow.update { IsLoadingState.NotLoading }
            } else {
                PassLogger.i(TAG, "Form errors: $errors")
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

    fun clearReason() {
        reportReasonFlow.update { None }
    }

    companion object {
        private const val TAG = "ReportViewModel"
    }
}


