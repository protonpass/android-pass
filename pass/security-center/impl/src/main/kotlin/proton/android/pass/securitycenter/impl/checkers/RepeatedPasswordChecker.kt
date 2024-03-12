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

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import javax.inject.Inject

data class RepeatedPasswordsData(
    val repeatedPasswordsCount: Int,
    val repeatedPasswords: Map<EncryptedString, List<Item>>
)

interface RepeatedPasswordChecker {
    suspend operator fun invoke(items: List<Item>): RepeatedPasswordsData
}

class RepeatedPasswordCheckerImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : RepeatedPasswordChecker {

    override suspend fun invoke(items: List<Item>): RepeatedPasswordsData {

        val decrypted: List<DecryptedItem> = coroutineScope {
            encryptionContextProvider.withEncryptionContextSuspendable {
                items.mapNotNull { item ->
                    when (val itemType = item.itemType) {
                        is ItemType.Login -> {
                            async {
                                DecryptedItem(
                                    item = item,
                                    encryptedPassword = Password(itemType.password),
                                    clearTextPassword = decrypt(itemType.password)
                                )
                            }
                        }
                        else -> null
                    }
                }
            }.awaitAll()
        }

        val foundPasswords: MutableMap<Password, List<Item>> = mutableMapOf()
        val passwordsWithMoreThanOneItem: MutableSet<Password> = mutableSetOf()

        decrypted.forEach { decryptedItem ->
            val list = foundPasswords.getOrElse(decryptedItem.encryptedPassword) { emptyList() }
            val updatedList = list + decryptedItem.item

            if (updatedList.size > 1) {
                passwordsWithMoreThanOneItem.add(decryptedItem.encryptedPassword)
            }

            foundPasswords[decryptedItem.encryptedPassword] = updatedList
        }

        val repeatedPasswords = foundPasswords
            .filterKeys { passwordsWithMoreThanOneItem.contains(it) }
            .mapKeys { it.key.value }

        return RepeatedPasswordsData(
            repeatedPasswordsCount = passwordsWithMoreThanOneItem.size,
            repeatedPasswords = repeatedPasswords
        )
    }

    @JvmInline
    private value class Password(val value: EncryptedString)

    private data class DecryptedItem(
        val item: Item,
        val encryptedPassword: Password,
        val clearTextPassword: String
    )
}
