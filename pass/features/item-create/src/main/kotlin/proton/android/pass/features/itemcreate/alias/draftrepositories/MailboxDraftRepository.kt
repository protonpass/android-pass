/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.draftrepositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import proton.android.pass.domain.AliasMailbox
import javax.inject.Inject

interface MailboxDraftRepository {
    fun addMailboxes(mailboxes: Set<AliasMailbox>)
    fun selectMailboxById(id: Int)
    fun getSelectedMailboxFlow(): Flow<Set<AliasMailbox>>
    fun getAllMailboxesFlow(): Flow<Set<AliasMailbox>>
    fun deselectMailboxById(id: Int)
    fun clearMailboxes()
}

class MailboxDraftRepositoryImpl @Inject constructor() : MailboxDraftRepository {
    private val mailboxes = MutableStateFlow<Set<AliasMailbox>>(emptySet())
    private val selectedMailboxIds = MutableStateFlow<Set<Int>>(emptySet())

    override fun addMailboxes(mailboxes: Set<AliasMailbox>) {
        this.mailboxes.update { current -> current + mailboxes }
    }

    override fun selectMailboxById(id: Int) {
        require(mailboxes.value.any { it.id == id }) { "Mailbox with id $id not found." }
        selectedMailboxIds.update { current -> current + id }
    }

    override fun deselectMailboxById(id: Int) {
        selectedMailboxIds.update { current -> current - id }
    }

    override fun clearMailboxes() {
        mailboxes.update { emptySet() }
        selectedMailboxIds.update { emptySet() }
    }

    override fun getSelectedMailboxFlow(): Flow<Set<AliasMailbox>> =
        combine(mailboxes, selectedMailboxIds) { currentMailboxes, currentSelectedIds ->
            currentMailboxes.filter { it.id in currentSelectedIds }
        }.map(List<AliasMailbox>::toSet)

    override fun getAllMailboxesFlow(): Flow<Set<AliasMailbox>> = mailboxes
}
