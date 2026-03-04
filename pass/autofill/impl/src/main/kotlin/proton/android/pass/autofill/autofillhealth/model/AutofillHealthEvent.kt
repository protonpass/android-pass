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

package proton.android.pass.autofill.autofillhealth.model

data class AutofillHealthEvent(
    val timestamp: Long,
    val type: AutofillHealthEventType,
    val packageName: String? = null,
    val details: String? = null
)

enum class AutofillHealthEventType {
    CREATED,
    CONNECTED,
    DISCONNECTED,
    FILL_REQUEST_INLINE,
    FILL_REQUEST_MENU,
    FILL_REQUEST_NONE,
    FILL_REQUEST_ERROR
}

data class LogcatEntry(
    val timestamp: String,
    val level: Char,
    val tag: String,
    val message: String,
    val isOwnProcess: Boolean = false,
    val processName: String = ""
)
