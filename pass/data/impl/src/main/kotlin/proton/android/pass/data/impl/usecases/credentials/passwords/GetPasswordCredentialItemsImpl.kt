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
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.credentials.passwords.GetPasswordCredentialItems
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.credentials.PasswordCredentialItem
import proton.android.pass.domain.toItemContents
import javax.inject.Inject

class GetPasswordCredentialItemsImpl @Inject constructor(
    private val observeAutofillShares: ObserveAutofillShares,
    private val observeItems: ObserveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : GetPasswordCredentialItems {

    override suspend fun invoke(packageName: String): List<PasswordCredentialItem> = observeAutofillShares()
        .flatMapLatest { autofillShares ->
            observeItems(
                selection = ShareSelection.Shares(autofillShares.map(Share::id)),
                itemState = ItemState.Active,
                filter = ItemTypeFilter.Logins
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
            loginItemContents.packageInfoSet.any { packageInfo ->
                packageInfo.packageName.value == packageName
            }
        }
        .map { loginItemContents ->
            PasswordCredentialItem(
                displayName = loginItemContents.title,
                username = loginItemContents.displayValue,
                encryptedPassword = loginItemContents.password.encrypted
            )
        }

}
