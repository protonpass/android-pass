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

package proton.android.pass.autofill.autofillhealth.service

import android.content.Context
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEvent
import proton.android.pass.autofill.autofillhealth.model.AutofillHealthEventType
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutofillHealthMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _events = MutableStateFlow<List<AutofillHealthEvent>>(emptyList())
    val events: StateFlow<List<AutofillHealthEvent>> = _events

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _currentIme = MutableStateFlow("")
    val currentIme: StateFlow<String> = _currentIme

    val lastFillRequestEvent: Flow<AutofillHealthEvent?> = _events.map { eventList ->
        eventList.lastOrNull { event ->
            event.type in FILL_REQUEST_TYPES
        }
    }

    fun recordCreate() {
        addEvent(AutofillHealthEvent(timestamp = now(), type = AutofillHealthEventType.CREATED))
    }

    fun recordConnect() {
        _isConnected.value = true
        addEvent(AutofillHealthEvent(timestamp = now(), type = AutofillHealthEventType.CONNECTED))
    }

    fun recordDisconnect() {
        _isConnected.value = false
        addEvent(AutofillHealthEvent(timestamp = now(), type = AutofillHealthEventType.DISCONNECTED))
    }

    fun recordFillRequest(packageName: String?, type: AutofillHealthEventType) {
        refreshIme()
        addEvent(
            AutofillHealthEvent(
                timestamp = now(),
                type = type,
                packageName = packageName
            )
        )
    }

    fun clearLog() {
        _events.value = emptyList()
    }

    private fun addEvent(event: AutofillHealthEvent) {
        _events.update { current ->
            (current + event).takeLast(MAX_EVENTS)
        }
    }

    private fun refreshIme() {
        val imeId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ).orEmpty()
        val imm = context.getSystemService(InputMethodManager::class.java)
        val imeInfo = imm?.enabledInputMethodList?.firstOrNull {
            it.id == imeId
        }
        val label = imeInfo?.loadLabel(context.packageManager)?.toString()
        _currentIme.value = label ?: imeId.substringAfterLast("/")
            .substringAfterLast(".")
    }

    private fun now(): Long = System.currentTimeMillis()

    private companion object {
        const val MAX_EVENTS = 100
        val FILL_REQUEST_TYPES = setOf(
            AutofillHealthEventType.FILL_REQUEST_INLINE,
            AutofillHealthEventType.FILL_REQUEST_MENU,
            AutofillHealthEventType.FILL_REQUEST_NONE,
            AutofillHealthEventType.FILL_REQUEST_ERROR
        )
    }
}
