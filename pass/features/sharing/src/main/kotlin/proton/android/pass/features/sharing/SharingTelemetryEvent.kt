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

package proton.android.pass.features.sharing

import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent

private const val KEY_TARGET_TYPE = "target_type"
private const val KEY_ITEM_TYPE = "item_type"
const val TARGET_TYPE_VAULT = "vault"
const val TARGET_TYPE_ITEM = "item"

data class InviteCreate(
    val targetType: String,
    val itemType: EventItemType? = null
) : DeferredTelemetryEvent("pass_invite.create") {
    override fun dimensions(): Map<String, String> = buildMap {
        put(KEY_TARGET_TYPE, targetType)
        itemType?.let { put(KEY_ITEM_TYPE, it.itemTypeName) }
    }
}

data class InviteAccept(
    val targetType: String,
    val itemType: EventItemType? = null
) : DeferredTelemetryEvent("pass_invite.accept") {
    override fun dimensions(): Map<String, String> = buildMap {
        put(KEY_TARGET_TYPE, targetType)
        itemType?.let { put(KEY_ITEM_TYPE, it.itemTypeName) }
    }
}

data class InviteReject(
    val targetType: String,
    val itemType: EventItemType? = null
) : DeferredTelemetryEvent("pass_invite.reject") {
    override fun dimensions(): Map<String, String> = buildMap {
        put(KEY_TARGET_TYPE, targetType)
        itemType?.let { put(KEY_ITEM_TYPE, it.itemTypeName) }
    }
}

data class InviteDelete(
    val targetType: String,
    val itemType: EventItemType? = null
) : DeferredTelemetryEvent("pass_invite.delete") {
    override fun dimensions(): Map<String, String> = buildMap {
        put(KEY_TARGET_TYPE, targetType)
        itemType?.let { put(KEY_ITEM_TYPE, it.itemTypeName) }
    }
}
