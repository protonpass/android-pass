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

package proton.android.pass.data.impl.usecases.credentials.passkeys

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.isReady
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.credentials.passkeys.GetPasskeyCredentialItems
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.data.impl.usecases.credentials.shared.getDisplayName
import proton.android.pass.domain.credentials.PasskeyCredentialItem
import javax.inject.Inject

class GetPasskeyCredentialItemsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val getPasskeysForDomain: GetPasskeysForDomain
) : GetPasskeyCredentialItems {

    override suspend fun invoke(domain: String, selection: PasskeySelection): List<PasskeyCredentialItem> {
        val accountsMap = accountManager.getAccounts()
            .map { accounts -> accounts.filter(Account::isReady) }
            .first()
            .associateBy(Account::userId)

        return accountsMap.values.map { account ->
            getPasskeysForDomain(
                domain = domain,
                selection = selection,
                userId = account.userId
            ).map { passkeyItem ->
                PasskeyCredentialItem(
                    passkeyItem = passkeyItem,
                    displayName = accountsMap.getDisplayName(account.userId, passkeyItem.itemTitle)
                )
            }
        }.flatten()
    }

}
