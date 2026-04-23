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

package proton.android.pass.protonapps.api

enum class ProtonAppType(
    val id: String,
    val playStoreFallbackUrl: String,
    val fdroidFallbackUrl: String?,
    val position: Int
) {
    Vpn(
        id = "ch.protonvpn.android",
        playStoreFallbackUrl = "https://play.google.com/store/apps/details?id=ch.protonvpn.android",
        fdroidFallbackUrl = "https://f-droid.org/packages/ch.protonvpn.android/",
        position = 0
    ),
    Mail(
        id = "ch.protonmail.android",
        playStoreFallbackUrl = "https://play.google.com/store/apps/details?id=ch.protonmail.android",
        fdroidFallbackUrl = null,
        position = 1
    ),
    Drive(
        id = "me.proton.android.drive",
        playStoreFallbackUrl = "https://play.google.com/store/apps/details?id=me.proton.android.drive",
        fdroidFallbackUrl = "https://f-droid.org/packages/me.proton.android.drive/",
        position = 2
    ),
    Lumo(
        id = "me.proton.android.lumo",
        playStoreFallbackUrl = "https://play.google.com/store/apps/details?id=me.proton.android.lumo",
        fdroidFallbackUrl = "https://f-droid.org/packages/me.proton.android.lumo/",
        position = 3
    ),
    Meet(
        id = "proton.android.meet",
        playStoreFallbackUrl = "https://play.google.com/store/apps/details?id=proton.android.meet",
        fdroidFallbackUrl = null,
        position = 4
    )
}
