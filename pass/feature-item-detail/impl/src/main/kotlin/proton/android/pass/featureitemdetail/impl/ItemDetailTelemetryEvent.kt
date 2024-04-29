/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemdetail.impl

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent

data class ItemDelete(val itemType: EventItemType) : TelemetryEvent("item.deletion") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}
data class ItemRead(val itemType: EventItemType) : TelemetryEvent("item.read") {
    override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
}

data object PassMonitorItemDetailFromWeakPassword :
    TelemetryEvent("pass_monitor.item_detail_from_weak_password")

data object PassMonitorItemDetailFromMissing2FA :
    TelemetryEvent("pass_monitor.item_detail_from_missing_2fa")

data object PassMonitorItemDetailFromReusedPassword :
    TelemetryEvent("pass_monitor.item_detail_from_reused_password")
