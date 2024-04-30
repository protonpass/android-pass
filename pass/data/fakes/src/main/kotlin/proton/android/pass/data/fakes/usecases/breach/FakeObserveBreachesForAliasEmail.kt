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

package proton.android.pass.data.fakes.usecases.breach

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForAliasEmail
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachEmail
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveBreachesForAliasEmail @Inject constructor() : ObserveBreachesForAliasEmail {

    private val flow: MutableStateFlow<List<BreachEmail>> = MutableStateFlow(emptyList())

    fun emit(emails: List<BreachEmail>) {
        flow.update { emails }
    }

    override fun invoke(shareId: ShareId, itemId: ItemId): Flow<List<BreachEmail>> = flow

}
