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

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonrust.api.PasswordScorer
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordsReport
import javax.inject.Inject

class InsecurePasswordCheckerImpl @Inject constructor(
    private val passwordScorer: PasswordScorer,
    private val encryptionContextProvider: EncryptionContextProvider
) : InsecurePasswordChecker {

    override suspend fun invoke(items: List<Item>): InsecurePasswordsReport {
        val weakItems = mutableListOf<Item>()
        val vulnerableItems = mutableListOf<Item>()
        encryptionContextProvider.withEncryptionContext {
            items.forEach {
                when (val itemType = it.itemType) {
                    is ItemType.Login -> when (val score = scorePassword(itemType.password)) {
                        is Some -> when (score.value) {
                            PasswordScore.VULNERABLE -> vulnerableItems.add(it)
                            PasswordScore.WEAK -> weakItems.add(it)
                            PasswordScore.STRONG -> {}
                        }

                        None -> {}
                    }

                    else -> {}
                }
            }
        }

        return InsecurePasswordsReport(
            weakPasswordItems = weakItems,
            vulnerablePasswordItems = vulnerableItems
        )
    }

    private fun EncryptionContext.scorePassword(password: EncryptedString): Option<PasswordScore> {
        val decryptedPassword = decrypt(password)
        if (decryptedPassword.isBlank()) return None

        return passwordScorer.check(decryptedPassword).some()
    }

}
