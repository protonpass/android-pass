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

package proton.android.pass.features.inappmessages

import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.telemetry.api.TelemetryEvent

private const val NOTIFICATION_KEY = "notificationKey"
private const val NOTIFICATION_STATUS = "notificationStatus"

data class InAppMessagesDisplay(val key: InAppMessageKey) :
    TelemetryEvent.DeferredTelemetryEvent("pass_notifications.display_notification") {
    override fun dimensions(): Map<String, String> = mapOf(NOTIFICATION_KEY to key.value)
}

data class InAppMessagesChange(val key: InAppMessageKey, val status: InAppMessageStatus) :
    TelemetryEvent.DeferredTelemetryEvent("pass_notifications.change_notification_status") {
    override fun dimensions(): Map<String, String> = buildMap {
        put(NOTIFICATION_KEY, key.value)
        status.toTelemetryEventName()?.let { put(NOTIFICATION_STATUS, it) }
    }
}

private fun InAppMessageStatus.toTelemetryEventName(): String? = when (this) {
    InAppMessageStatus.Unread -> "unread"
    InAppMessageStatus.Dismissed -> "dismissed"
    InAppMessageStatus.Read -> "read"
    InAppMessageStatus.Unknown -> null
}

data class InAppMessagesClick(val key: InAppMessageKey) :
    TelemetryEvent.DeferredTelemetryEvent("pass_notifications.notification_cta_click") {
    override fun dimensions(): Map<String, String> = mapOf(NOTIFICATION_KEY to key.value)
}
