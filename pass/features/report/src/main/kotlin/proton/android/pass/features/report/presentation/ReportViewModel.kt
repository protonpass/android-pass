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

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.user.domain.UserManager
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.report.Report
import proton.android.pass.data.api.usecases.report.SendReport
import proton.android.pass.features.report.presentation.ReportFormData.Companion.validate
import proton.android.pass.features.report.ui.ReportReason
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.passkeys.api.CheckPasskeySupport
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandleProvider: SavedStateHandleProvider,
    accountManager: AccountManager,
    userManager: UserManager,
    private val autofillManager: AutofillManager,
    private val sendReport: SendReport,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val checkPasskeySupport: CheckPasskeySupport,
    private val emailValidator: EmailValidator
) : ViewModel() {

    @OptIn(SavedStateHandleSaveableApi::class)
    var formState by savedStateHandleProvider.get().saveable { mutableStateOf(ReportFormData()) }

    init {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
            val user = userId?.let { userManager.getUser(it) }
            val email = user?.email ?: "unknown"
            val username = user?.name ?: userId?.id ?: "unknown"
            formState = ReportFormData(email = email, username = username)
        }
    }

    private val reportEventFlow: MutableStateFlow<ReportEvent> = MutableStateFlow(ReportEvent.Idle)
    private val reportReasonFlow: MutableStateFlow<Option<ReportReason>> = MutableStateFlow(None)
    private val isLoadingStateFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val formValidationErrorsStateFlow =
        MutableStateFlow(persistentListOf<ReportValidationError>())

    internal val state = combine(
        oneShot { checkPasskeySupport().some() },
        reportEventFlow,
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
            val errors: List<ReportValidationError> = formState.validate(emailValidator)
            formValidationErrorsStateFlow.update { errors.toPersistentList() }
            if (errors.isEmpty()) {
                isLoadingStateFlow.update { IsLoadingState.Loading }
                runCatching {
                    val title =
                        "Report from Pass Android: ${reportReasonFlow.value.value() ?: "Empty"}"
                    val extraFiles = formState.extraFiles.mapIndexed { index, uri ->
                        fileFromContentUri(context, index, uri)
                    }.toSet()
                    val report = Report(
                        title = title,
                        description = formState.description,
                        email = formState.email,
                        username = formState.username,
                        shouldAttachLog = formState.attachLog,
                        extraFiles = extraFiles
                    )
                    sendReport(report)
                }
                    .onSuccess {
                        PassLogger.i(TAG, "Report sent successfully")
                        reportEventFlow.update { ReportEvent.Close }
                        snackbarDispatcher(ReportSnackbarMessage.ReportSendingSuccess)
                    }
                    .onError {
                        PassLogger.w(TAG, "Error sending report")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ReportSnackbarMessage.ReportSendingError)
                    }
                isLoadingStateFlow.update { IsLoadingState.NotLoading }
            } else {
                PassLogger.i(TAG, "Form errors: $errors")
            }
        }
    }

    private suspend fun fileFromContentUri(
        context: Context,
        index: Int,
        uri: Uri
    ): File = withContext(Dispatchers.IO) {
        val fileType = context.contentResolver.getType(uri)
        val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
        val fileName = "extra_report_file_$index${fileExtension?.let { ".$it" } ?: ""}"
        val tempFile = File(context.cacheDir, fileName).apply { createNewFile() }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
            }
        }
        tempFile
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

    fun onImagesSelected(value: Set<Uri>) {
        formState = formState.copy(
            extraFiles = formState.extraFiles + value
        )
    }

    fun onImageRemoved(value: Uri) {
        formState = formState.copy(
            extraFiles = formState.extraFiles - value
        )
    }

    fun onEventConsumed(event: ReportEvent) {
        reportEventFlow.compareAndSet(event, ReportEvent.Idle)
    }

    companion object {
        private const val TAG = "ReportViewModel"

        private const val BUFFER_SIZE = 8192
    }
}

sealed interface ReportEvent {
    data object Idle : ReportEvent
    data object Close : ReportEvent
}
