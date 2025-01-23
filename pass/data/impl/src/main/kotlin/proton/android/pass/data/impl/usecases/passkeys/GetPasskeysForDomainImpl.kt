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
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.passkeys.GetPasskeysForDomain
import proton.android.pass.data.api.usecases.passkeys.ObserveItemsWithPasskeys
import proton.android.pass.data.api.usecases.passkeys.PasskeyItem
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.data.api.usecases.shares.ObserveAutofillShares
import proton.android.pass.data.impl.util.DomainUtils
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPasskeysForDomainImpl @Inject constructor(
    private val observeItemsWithPasskeys: ObserveItemsWithPasskeys,
    private val observeAutofillShares: ObserveAutofillShares,
    private val encryptionContextProvider: EncryptionContextProvider
) : GetPasskeysForDomain {

    override suspend fun invoke(domain: String, selection: PasskeySelection): List<PasskeyItem> {
        val parsed = UrlSanitizer.getDomain(domain).getOrElse {
            PassLogger.w(TAG, "Could not get domain from $domain")
            return emptyList()
        }

        val allItemsWithPasskeys = observeAutofillShares().flatMapLatest {
            observeItemsWithPasskeys(shareSelection = ShareSelection.Shares(it.map { share -> share.id }))
        }.first()

        val loginItems = encryptionContextProvider.withEncryptionContext {
            allItemsWithPasskeys
                .filter {
                    when (val itemType = it.itemType) {
                        is ItemType.Login -> itemType.passkeys.isNotEmpty()
                        else -> false
                    }
                }
                .map {
                    LoginItem(
                        shareId = it.shareId,
                        itemId = it.id,
                        login = it.itemType as ItemType.Login,
                        itemTitle = decrypt(it.title)
                    )
                }
        }

        val passkeysForDomain = loginItems.mapNotNull { item ->
            val domainPasskeys = item.login.passkeys.filter {
                val passkeyDomain = UrlSanitizer.getDomain(it.domain).getOrElse {
                    return@filter false
                }

                DomainUtils.isDomainPartOf(needle = parsed, haystack = passkeyDomain)
            }

            val allowedPasskeys = when (selection) {
                is PasskeySelection.Allowed -> domainPasskeys.filter { passkey ->
                    selection.allowedPasskeys.any { it == passkey.id }
                }

                PasskeySelection.All -> domainPasskeys
            }

            if (allowedPasskeys.isEmpty()) {
                null
            } else {
                allowedPasskeys.map {
                    PasskeyItem(
                        shareId = item.shareId,
                        itemId = item.itemId,
                        passkey = it,
                        itemTitle = item.itemTitle
                    )
                }
            }
        }.flatten()

        // Sort them by creation date
        return passkeysForDomain.sortedByDescending { it.passkey.createTime }
    }

    private data class LoginItem(
        val shareId: ShareId,
        val itemId: ItemId,
        val login: ItemType.Login,
        val itemTitle: String
    )

    private companion object {

        private const val TAG = "GetPasskeysForDomainImpl"

    }

}
