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

package proton.android.pass.data.impl.usecases.aliascontact

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.AliasContactsRepository
import proton.android.pass.data.api.usecases.aliascontact.ObserveAliasContact
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.aliascontacts.Contact
import proton.android.pass.domain.aliascontacts.ContactId
import javax.inject.Inject

class ObserveAliasContactImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val aliasContactsRepository: AliasContactsRepository
) : ObserveAliasContact {

    override fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        contactId: ContactId
    ): Flow<Contact> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest {
            aliasContactsRepository.observeAliasContact(
                it,
                shareId,
                itemId,
                contactId
            )
        }
}
