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

package proton.android.pass.securitycenter.impl.checkers

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.securitycenter.impl.helpers.Supports2fa
import javax.inject.Inject

data class Missing2faReport(
    val items: List<Item>
) {
    val missing2faCount: Int = items.size
}

interface Missing2faChecker {
    suspend operator fun invoke(items: List<Item>): Missing2faReport
}

class Missing2faCheckerImpl @Inject constructor(
    private val supports2fa: Supports2fa,
    private val encryptionContextProvider: EncryptionContextProvider
) : Missing2faChecker {
    override suspend fun invoke(items: List<Item>): Missing2faReport {
        val itemsToReport = encryptionContextProvider.withEncryptionContext {
            items.filter { shouldReportItem(it) }
        }

        return Missing2faReport(itemsToReport)
    }

    private fun EncryptionContext.shouldReportItem(item: Item): Boolean {
        val loginItemType = when (val itemType = item.itemType) {
            is ItemType.Login -> itemType
            else -> return false
        }

        val decryptedTotp = decrypt(loginItemType.primaryTotp)
        if (decryptedTotp.isNotEmpty()) return false

        return isAnyWebsiteEligible(loginItemType.websites)
    }

    private fun isAnyWebsiteEligible(websites: List<String>): Boolean = runCatching {
        websites.any { website ->
            UrlSanitizer.getDomain(website).fold(
                onSuccess = { domain ->
                    supports2fa(domain)
                },
                onFailure = {
                    supports2fa(website)
                }
            )
        }
    }.getOrElse {
        PassLogger.w(TAG, "Error checking twofaDomainChecker")
        PassLogger.w(TAG, it)
        false
    }

    companion object {
        private const val TAG = "Missing2faCheckerImpl"
    }
}
