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

package proton.android.pass.data.impl.usecases.passkeys

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.usecases.ObserveUsableVaults
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Passkey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPasskeysForDomainImpl @Inject constructor(
    private val observeItemsWithPasskeys: ObserveItemsWithPasskeys,
    private val observeUsableVaults: ObserveUsableVaults
) : GetPasskeysForDomain {
    override suspend fun invoke(domain: String): List<Passkey> {
        val allItemsWithPasskeys = observeUsableVaults().flatMapLatest {
            observeItemsWithPasskeys(it)
        }.first()

        return allItemsWithPasskeys
            .filter { it.itemType is ItemType.Login }
            .map { it.itemType as ItemType.Login }
            .flatMap { loginItem -> loginItem.passkeys }
            .filter { passkey -> passkey.domain == domain }
    }
}
