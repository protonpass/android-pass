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

package proton.android.pass.features.secure.links.shared.presentation

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import proton.android.pass.domain.securelinks.SecureLinkExpiration
import proton.android.pass.features.secure.links.R

internal object SecureLink {

    internal val expirationOptionsMap: ImmutableMap<SecureLinkExpiration, Int> = mapOf(
        SecureLinkExpiration.OneHour to R.string.secure_links_create_row_expiration_options_one_hour,
        SecureLinkExpiration.OneDay to R.string.secure_links_create_row_expiration_options_one_day,
        SecureLinkExpiration.SevenDays to R.string.secure_links_create_row_expiration_options_seven_days,
        SecureLinkExpiration.FourteenDays to R.string.secure_links_create_row_expiration_options_fourteen_days,
        SecureLinkExpiration.ThirtyDays to R.string.secure_links_create_row_expiration_options_thirty_days
    ).toPersistentMap()

}
