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

package proton.android.pass.data.impl.usecases.credentials.passwords

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.isReady
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.api.usecases.credentials.passwords.GetPasswordCredentialItems
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.data.impl.usecases.credentials.shared.getDisplayName
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.domain.toItemContents
import javax.inject.Inject

class GetPasswordCredentialItemsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val observeAutofillShares: ObserveAutofillShares,
    private val observeItems: ObserveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : GetPasswordCredentialItems {

    override suspend fun invoke(suggestion: Suggestion): List<PasswordCredentialItem> {
        val accountsMap = accountManager.getAccounts()
            .map { accounts -> accounts.filter(Account::isReady) }
            .first()
            .associateBy(Account::userId)

        return accountsMap.values.map { account ->
            observeAutofillShares(userId = account.userId)
                .first()
                .map(Share::id)
                .let { shareIds ->
                    observeItems(
                        userId = account.userId,
                        selection = ShareSelection.Shares(shareIds),
                        itemState = ItemState.Active,
                        filter = ItemTypeFilter.Logins,
                        includeHidden = false
                    )
                }
                .first()
                .let { loginItems ->
                    encryptionContextProvider.withEncryptionContextSuspendable {
                        loginItems.map { loginItem ->
                            loginItem.toItemContents<ItemContents.Login> { decrypt(it) }
                        }
                    }
                }
                .filter { loginItemContents ->
                    when (suggestion) {
                        is Suggestion.PackageName -> {
                            loginItemContents.packageInfoSet.any { packageInfo ->
                                packageInfo.packageName.value == suggestion.value
                            }
                        }

                        is Suggestion.Url -> {
                            loginItemContents.urls.any { url ->
                                url == suggestion.value
                            }
                        }
                    }
                }
                .filter { it.displayValue.isNotBlank() && it.password !is HiddenState.Empty }
                .map { loginItemContents ->
                    PasswordCredentialItem(
                        displayName = accountsMap.getDisplayName(account.userId, loginItemContents.title),
                        username = loginItemContents.displayValue,
                        encryptedPassword = loginItemContents.password.encrypted
                    )
                }
        }.flatten()
    }

}
