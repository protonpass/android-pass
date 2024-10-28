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

package proton.android.pass.features.alias.contacts.detail.presentation

import proton.android.pass.domain.aliascontacts.ContactId

sealed interface DetailAliasContactUIEvent {
    data object Back : DetailAliasContactUIEvent
    data object CreateContact : DetailAliasContactUIEvent
    data object Help : DetailAliasContactUIEvent
    data object LearnMore : DetailAliasContactUIEvent
    data object Upgrade : DetailAliasContactUIEvent

    @JvmInline
    value class SendEmail(val email: String) : DetailAliasContactUIEvent

    @JvmInline
    value class ContactOptions(val contactId: ContactId) : DetailAliasContactUIEvent

    @JvmInline
    value class BlockContact(val contactId: ContactId) : DetailAliasContactUIEvent

    @JvmInline
    value class UnblockContact(val contactId: ContactId) : DetailAliasContactUIEvent

}
