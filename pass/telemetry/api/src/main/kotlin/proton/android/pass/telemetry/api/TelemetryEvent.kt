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

package proton.android.pass.telemetry.api

import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password"),
    CreditCard("credit_card"),
    Identity("identity"),
    Custom("custom"),
    Unknown("unknown");

    companion object {
        fun from(itemType: ItemType): EventItemType = when (itemType) {
            ItemType.Unknown -> Unknown
            ItemType.Password -> Password
            is ItemType.Alias -> Alias
            is ItemType.Note -> Note
            is ItemType.Login -> Login
            is ItemType.CreditCard -> CreditCard
            is ItemType.Identity -> Identity
            is ItemType.Custom -> Custom
        }

        fun from(itemContents: ItemContents): EventItemType = when (itemContents) {
            is ItemContents.Alias -> Alias
            is ItemContents.Login -> Login
            is ItemContents.Note -> Note
            is ItemContents.CreditCard -> CreditCard
            is ItemContents.Identity -> Identity
            is ItemContents.Custom -> Custom
            is ItemContents.Unknown -> Unknown
        }
    }
}

sealed class TelemetryEvent(val eventName: String) {
    open fun dimensions(): Map<String, String> = emptyMap()

    @Suppress("UnnecessaryAbstractClass")
    abstract class DeferredTelemetryEvent(eventName: String) : TelemetryEvent(eventName)

    @Suppress("UnnecessaryAbstractClass")
    abstract class LiveTelemetryEvent(eventName: String) : TelemetryEvent(eventName)

}
