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

package proton.android.pass.commonrust.impl

import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.VaultProperties
import proton.android.pass.types.VaultColor as LibraryVaultColor
import proton.android.pass.types.VaultData as LibraryVaultData
import proton.android.pass.types.VaultDisplayPreferences as LibraryVaultDisplayPreferences
import proton.android.pass.types.VaultIcon as LibraryVaultIcon

private val ICON_TO_LIBRARY: Map<ShareIcon, LibraryVaultIcon> = mapOf(
    ShareIcon.Icon1 to LibraryVaultIcon.ICON1,
    ShareIcon.Icon2 to LibraryVaultIcon.ICON2,
    ShareIcon.Icon3 to LibraryVaultIcon.ICON3,
    ShareIcon.Icon4 to LibraryVaultIcon.ICON4,
    ShareIcon.Icon5 to LibraryVaultIcon.ICON5,
    ShareIcon.Icon6 to LibraryVaultIcon.ICON6,
    ShareIcon.Icon7 to LibraryVaultIcon.ICON7,
    ShareIcon.Icon8 to LibraryVaultIcon.ICON8,
    ShareIcon.Icon9 to LibraryVaultIcon.ICON9,
    ShareIcon.Icon10 to LibraryVaultIcon.ICON10,
    ShareIcon.Icon11 to LibraryVaultIcon.ICON11,
    ShareIcon.Icon12 to LibraryVaultIcon.ICON12,
    ShareIcon.Icon13 to LibraryVaultIcon.ICON13,
    ShareIcon.Icon14 to LibraryVaultIcon.ICON14,
    ShareIcon.Icon15 to LibraryVaultIcon.ICON15,
    ShareIcon.Icon16 to LibraryVaultIcon.ICON16,
    ShareIcon.Icon17 to LibraryVaultIcon.ICON17,
    ShareIcon.Icon18 to LibraryVaultIcon.ICON18,
    ShareIcon.Icon19 to LibraryVaultIcon.ICON19,
    ShareIcon.Icon20 to LibraryVaultIcon.ICON20,
    ShareIcon.Icon21 to LibraryVaultIcon.ICON21,
    ShareIcon.Icon22 to LibraryVaultIcon.ICON22,
    ShareIcon.Icon23 to LibraryVaultIcon.ICON23,
    ShareIcon.Icon24 to LibraryVaultIcon.ICON24,
    ShareIcon.Icon25 to LibraryVaultIcon.ICON25,
    ShareIcon.Icon26 to LibraryVaultIcon.ICON26,
    ShareIcon.Icon27 to LibraryVaultIcon.ICON27,
    ShareIcon.Icon28 to LibraryVaultIcon.ICON28,
    ShareIcon.Icon29 to LibraryVaultIcon.ICON29,
    ShareIcon.Icon30 to LibraryVaultIcon.ICON30
)

private val LIBRARY_TO_ICON: Map<LibraryVaultIcon, ShareIcon> = ICON_TO_LIBRARY.entries.associate { it.value to it.key }

private val COLOR_TO_LIBRARY: Map<ShareColor, LibraryVaultColor> = mapOf(
    ShareColor.Color1 to LibraryVaultColor.COLOR1,
    ShareColor.Color2 to LibraryVaultColor.COLOR2,
    ShareColor.Color3 to LibraryVaultColor.COLOR3,
    ShareColor.Color4 to LibraryVaultColor.COLOR4,
    ShareColor.Color5 to LibraryVaultColor.COLOR5,
    ShareColor.Color6 to LibraryVaultColor.COLOR6,
    ShareColor.Color7 to LibraryVaultColor.COLOR7,
    ShareColor.Color8 to LibraryVaultColor.COLOR8,
    ShareColor.Color9 to LibraryVaultColor.COLOR9,
    ShareColor.Color10 to LibraryVaultColor.COLOR10
)

private val LIBRARY_TO_COLOR: Map<LibraryVaultColor, ShareColor> =
    COLOR_TO_LIBRARY.entries.associate { it.value to it.key }

internal fun ShareIcon.toLibrary(): LibraryVaultIcon = ICON_TO_LIBRARY.getValue(this)

internal fun LibraryVaultIcon.toDomain(): ShareIcon = LIBRARY_TO_ICON[this] ?: ShareIcon.Icon1

internal fun ShareColor.toLibrary(): LibraryVaultColor = COLOR_TO_LIBRARY.getValue(this)

internal fun LibraryVaultColor.toDomain(): ShareColor = LIBRARY_TO_COLOR[this] ?: ShareColor.Color1

internal fun VaultProperties.toLibraryVaultData(existing: LibraryVaultData? = null): LibraryVaultData {
    val existingPreferences = existing?.displayPreferences
    val libraryIcon = existingPreferences?.icon?.takeIf { it.toDomain() == icon } ?: icon.toLibrary()
    val libraryColor = existingPreferences?.color?.takeIf { it.toDomain() == color } ?: color.toLibrary()
    return LibraryVaultData(
        name = name,
        description = description,
        displayPreferences = LibraryVaultDisplayPreferences(icon = libraryIcon, color = libraryColor)
    )
}

internal fun LibraryVaultData.toVaultProperties(): VaultProperties = VaultProperties(
    name = name,
    description = description,
    icon = displayPreferences.icon.toDomain(),
    color = displayPreferences.color.toDomain()
)
