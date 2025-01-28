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

package proton.android.pass.data.impl.extensions

import proton.android.pass.data.impl.responses.AliasMailboxResponse
import proton.android.pass.data.impl.responses.AliasOptionsResponse
import proton.android.pass.data.impl.responses.AliasSuffixResponse
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.AliasSuffix

fun AliasOptionsResponse.toDomain(): AliasOptions = AliasOptions(
    suffixes = suffixes.map { it.toDomain() },
    mailboxes = mailboxes.map { it.toDomain() }
)

fun AliasSuffixResponse.toDomain(): AliasSuffix = AliasSuffix(
    suffix = suffix,
    signedSuffix = signedSuffix,
    isCustom = isCustom,
    isPremium = isPremium,
    domain = domain
)

fun AliasMailboxResponse.toDomain(): AliasMailbox = AliasMailbox(
    id = id,
    email = email
)
