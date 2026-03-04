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

package proton.android.pass.autofill.autofillhealth.viewmodel

import android.content.Context
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.LogcatEntry
import proton.android.pass.autofill.autofillhealth.service.AutofillHealthMonitor
import proton.android.pass.autofill.autofillhealth.service.AutofillHealthOverlay
import proton.android.pass.autofill.autofillhealth.service.AutofillLogcatReader
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.log.api.PassLogger
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AutofillHealthDebugViewModel @Inject constructor(
    private val monitor: AutofillHealthMonitor,
    private val overlay: AutofillHealthOverlay,
    private val logcatReader: AutofillLogcatReader,
    private val fileHandler: FileHandler,
    private val appDispatchers: AppDispatchers
) : ViewModel() {

    private val _isOverlayVisible = MutableStateFlow(overlay.isVisible)
    private val _refreshTrigger = MutableStateFlow(0L)

    init {
        logcatReader.start()
    }

    val state: StateFlow<AutofillHealthDebugUiState> = combine(
        monitor.isConnected,
        monitor.lastFillRequestEvent,
        monitor.currentIme,
        monitor.events,
        _isOverlayVisible,
        logcatReader.entries,
        logcatReader.hasReadLogsPermission,
        logcatReader.isVerbosePropsEnabled,
        _refreshTrigger
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        AutofillHealthDebugUiState(
            isConnected = values[0] as Boolean,
            lastFillRequest = values[1] as? AutofillHealthEvent,
            currentIme = values[2] as String,
            events = (values[3] as List<AutofillHealthEvent>).reversed(),
            hasOverlayPermissionInManifest = overlay.hasPermissionInManifest,
            canShowOverlay = overlay.canShow,
            isOverlayVisible = values[4] as Boolean,
            logcatEntries = (values[5] as List<LogcatEntry>).reversed(),
            hasReadLogsPermission = values[6] as Boolean,
            isVerbosePropsEnabled = values[7] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutofillHealthDebugUiState(
            hasOverlayPermissionInManifest = overlay.hasPermissionInManifest,
            canShowOverlay = overlay.canShow
        )
    )

    fun refreshPermissions() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    fun clearLog() {
        monitor.clearLog()
    }

    fun clearLogcat() {
        logcatReader.clear()
    }

    fun refreshLogcatState() {
        logcatReader.refreshState()
    }

    fun toggleOverlay() {
        val newVisible = !_isOverlayVisible.value
        _isOverlayVisible.value = newVisible
        if (newVisible) {
            overlay.show()
        } else {
            overlay.hide()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun shareEvents(context: Context) = viewModelScope.launch(appDispatchers.io) {
        val events = monitor.events.value
        if (events.isEmpty()) return@launch
        try {
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val tsFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            val fileName = "autofill_events_${dateFormat.format(Date())}.log"
            val file = File(shareDir, fileName)

            file.bufferedWriter().use { writer ->
                events.forEach { event ->
                    val ts = tsFormat.format(Date(event.timestamp))
                    val pkg = event.packageName?.let { " [$it]" }.orEmpty()
                    writer.append("$ts ${event.type.name}$pkg")
                    writer.newLine()
                }
            }

            fileHandler.shareFile(
                contextHolder = ClassHolder(Some(WeakReference(context))),
                fileTitle = fileName,
                uri = file.toURI(),
                mimeType = "text/plain",
                chooserTitle = "Share autofill events"
            )
        } catch (e: IOException) {
            PassLogger.w(TAG, e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun shareLogcat(context: Context) = viewModelScope.launch(appDispatchers.io) {
        val entries = logcatReader.entries.value
        if (entries.isEmpty()) return@launch
        try {
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
            val fileName = "autofill_logcat_${dateFormat.format(Date())}.log"
            val file = File(shareDir, fileName)

            file.bufferedWriter().use { writer ->
                entries.forEach { entry ->
                    val source = if (entry.isOwnProcess) {
                        "APP"
                    } else {
                        "SYS" + if (entry.processName.isNotEmpty()) ":${entry.processName}" else ""
                    }
                    writer.append("[$source] ${entry.timestamp} ${entry.level} ${entry.tag}: ${entry.message}")
                    writer.newLine()
                }
            }

            fileHandler.shareFile(
                contextHolder = ClassHolder(Some(WeakReference(context))),
                fileTitle = fileName,
                uri = file.toURI(),
                mimeType = "text/plain",
                chooserTitle = "Share autofill logcat"
            )
        } catch (e: IOException) {
            PassLogger.w(TAG, e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        logcatReader.stop()
    }

    private companion object {
        const val TAG = "AutofillHealthDebugVM"
    }
}

data class AutofillHealthDebugUiState(
    val isConnected: Boolean = false,
    val lastFillRequest: AutofillHealthEvent? = null,
    val currentIme: String = "",
    val events: List<AutofillHealthEvent> = emptyList(),
    val hasOverlayPermissionInManifest: Boolean = false,
    val canShowOverlay: Boolean = false,
    val isOverlayVisible: Boolean = false,
    val logcatEntries: List<LogcatEntry> = emptyList(),
    val hasReadLogsPermission: Boolean = false,
    val isVerbosePropsEnabled: Boolean = false
)
