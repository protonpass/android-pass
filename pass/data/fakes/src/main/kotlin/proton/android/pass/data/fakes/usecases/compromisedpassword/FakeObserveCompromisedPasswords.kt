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

package proton.android.pass.data.fakes.usecases.compromisedpassword

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.CompromisedPasswordItem
import proton.android.pass.data.api.usecases.compromisedpassword.ObserveCompromisedPasswords
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveCompromisedPasswords @Inject constructor() : ObserveCompromisedPasswords {

    private val flow = MutableStateFlow<List<CompromisedPasswordItem>>(emptyList())

    fun emit(items: List<CompromisedPasswordItem>) {
        flow.value = items
    }

    override fun invoke(): Flow<List<CompromisedPasswordItem>> = flow

    override fun invoke(userId: UserId): Flow<List<CompromisedPasswordItem>> = flow

    override fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Boolean> = flow.map { items -> items.any { it.shareId == shareId && it.itemId == itemId } }
}
