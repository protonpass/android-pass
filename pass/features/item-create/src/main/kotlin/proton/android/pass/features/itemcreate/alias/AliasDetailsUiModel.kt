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

package proton.android.pass.features.itemcreate.alias

import proton.android.pass.domain.AliasDetails

data class AliasDetailsUiModel(
    val email: String,
    val mailboxes: List<AliasMailboxUiModel>,
    val availableMailboxes: List<AliasMailboxUiModel>,
    val stats: AliasStatsUiModel,
    val slNote: String,
    val name: String?
) {
    constructor(aliasDetails: AliasDetails) : this(
        email = aliasDetails.email,
        mailboxes = aliasDetails.mailboxes.map(::AliasMailboxUiModel),
        availableMailboxes = aliasDetails.availableMailboxes.map(::AliasMailboxUiModel),
        stats = aliasDetails.stats.let(::AliasStatsUiModel),
        slNote = aliasDetails.slNote,
        name = aliasDetails.name
    )
}
