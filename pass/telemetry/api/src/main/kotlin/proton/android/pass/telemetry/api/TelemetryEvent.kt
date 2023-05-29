package proton.android.pass.telemetry.api

import proton.pass.domain.ItemContents
import proton.pass.domain.ItemType

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password"),
    Unknown("unknown");

    companion object {
        fun from(itemType: ItemType): EventItemType = when (itemType) {
            ItemType.Unknown -> Unknown
            ItemType.Password -> Password
            is ItemType.Alias -> Alias
            is ItemType.Note -> Note
            is ItemType.Login -> Login
        }

        fun from(itemContents: ItemContents): EventItemType = when (itemContents) {
            is ItemContents.Alias -> Alias
            is ItemContents.Login -> Login
            is ItemContents.Note -> Note
            is ItemContents.Unknown -> Unknown
        }
    }
}

@Suppress("UnnecessaryAbstractClass")
abstract class TelemetryEvent(val eventName: String) {
    open fun dimensions(): Map<String, String> = emptyMap()
}
