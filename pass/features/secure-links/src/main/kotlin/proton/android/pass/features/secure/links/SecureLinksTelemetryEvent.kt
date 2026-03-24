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

package proton.android.pass.features.secure.links

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent

private const val KEY_ITEM_TYPE = "item_type"

data class SecureLinkCreate(val itemType: EventItemType) :
    DeferredTelemetryEvent("pass_secure_link.create") {
    override fun dimensions(): Map<String, String> = mapOf(KEY_ITEM_TYPE to itemType.itemTypeName)
}

data class SecureLinkDelete(val itemType: EventItemType) :
    DeferredTelemetryEvent("pass_secure_link.delete") {
    override fun dimensions(): Map<String, String> = mapOf(KEY_ITEM_TYPE to itemType.itemTypeName)
}

data object SecureInactiveLinkDelete : DeferredTelemetryEvent("pass_secure_link.delete_inactives")

