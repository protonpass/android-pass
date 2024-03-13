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
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import javax.inject.Inject

data class RepeatedPasswordsData(
    val repeatedPasswords: Map<EncryptedString, List<Item>>
) {
    val repeatedPasswordsCount: Int = repeatedPasswords.size
}

interface RepeatedPasswordChecker {
    operator fun invoke(items: List<Item>): RepeatedPasswordsData
}

class RepeatedPasswordCheckerImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : RepeatedPasswordChecker {

    override fun invoke(items: List<Item>): RepeatedPasswordsData {
        val foundPasswords: MutableMap<Password, List<Item>> = mutableMapOf()

        encryptionContextProvider.withEncryptionContext {
            items.forEach { item ->
                when (val itemType = item.itemType) {
                    is ItemType.Login -> {
                        val decryptedItem = DecryptedItem(
                            item = item,
                            encryptedPassword = Password(itemType.password),
                            clearTextPassword = decrypt(itemType.password)
                        )

                        if (decryptedItem.clearTextPassword.isNotEmpty()) {
                            val list = foundPasswords.getOrElse(decryptedItem.encryptedPassword) {
                                emptyList()
                            }
                            foundPasswords[decryptedItem.encryptedPassword] = list + decryptedItem.item
                        }
                    }
                    else -> {}
                }
            }
        }

        val repeatedPasswords = foundPasswords
            .filter { it.value.size > 1 }
            .mapKeys { it.key.value }

        return RepeatedPasswordsData(repeatedPasswords = repeatedPasswords)
    }

    @JvmInline
    private value class Password(val value: EncryptedString)

    private data class DecryptedItem(
        val item: Item,
        val encryptedPassword: Password,
        val clearTextPassword: String
    )
}
