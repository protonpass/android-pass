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

package proton.android.pass.features.secure.links.list.presentation

import kotlinx.datetime.Instant
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.domain.time.RemainingTime

internal data class SecureLinkModel(
    internal val itemTitle: String,
    private val itemType: ItemType,
    private val secureLink: SecureLink
) {

    internal val secureLinkId: SecureLinkId = secureLink.id

    internal val itemCategory: ItemCategory = itemType.category

    internal val itemWebsite: String? = when (itemType) {
        is ItemType.Login -> itemType.websites.firstOrNull()
        is ItemType.Alias,
        is ItemType.CreditCard,
        is ItemType.Identity,
        is ItemType.Note,
        is ItemType.WifiNetwork,
        is ItemType.SSHKey,
        is ItemType.Custom,
        ItemType.Password,
        ItemType.Unknown -> null
    }

    internal val itemPackageName: String? = when (itemType) {
        is ItemType.Login -> itemType.packageInfoSet.firstOrNull()?.packageName?.value
        is ItemType.Alias,
        is ItemType.CreditCard,
        is ItemType.Identity,
        is ItemType.Note,
        is ItemType.WifiNetwork,
        is ItemType.SSHKey,
        is ItemType.Custom,
        ItemType.Password,
        ItemType.Unknown -> null
    }

    internal val views: Int = secureLink.readCount

    internal val remainingTime: RemainingTime = RemainingTime(
        endInstant = Instant.fromEpochSeconds(secureLink.expirationInSeconds)
    )

    internal val isActive: Boolean = secureLink.isActive

    internal val hasReachedMaxViewsLimit: Boolean = secureLink.maxReadCount
        ?.let { maxViewsLimit -> maxViewsLimit == views }
        ?: false

}
