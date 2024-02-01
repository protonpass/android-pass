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

package proton.android.pass.passkeys.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.passkeys.api.PasskeyManager
import proton.android.pass.passkeys.api.PasskeySummary
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasskeyManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PasskeyManager {

    override suspend fun storePasskey(content: ByteArray, domain: String, identity: String) {
        val passkeys = loadPasskeys().toMutableList()
        val id = "${domain}_$identity"
        val passkey = PasskeyEntity(
            content = content,
            domain = domain,
            title = identity,
            id = id
        )
        passkeys.add(passkey)
        storePasskeys(passkeys)
    }

    override suspend fun getPasskeysForDomain(domain: String): List<PasskeySummary> {
        val passkeys = loadPasskeys()
        return passkeys
            .filter { it.domain == domain }
            .map {
                PasskeySummary(
                    id = it.id,
                    shareId = it.id,
                    itemId = it.id,
                    title = it.title
                )
            }
    }

    override suspend fun getPasskeyById(id: String): Option<ByteArray> {
        val passkeys = loadPasskeys()
        return passkeys.firstOrNull { it.id == id }?.content.toOption()
    }

    override suspend fun clearPasskeys() {
        storePasskeys(emptyList())
    }

    private suspend fun storePasskeys(passkeys: List<PasskeyEntity>) = withContext(Dispatchers.IO) {
        val file = getStorageFile()
        val content = Json.encodeToString(passkeys)
        file.writeText(content)
    }

    private suspend fun loadPasskeys(): List<PasskeyEntity> = withContext(Dispatchers.IO) {
        val file = getStorageFile()
        val content = file.readText()
        if (content.isBlank()) {
            emptyList()
        } else {
            val passkeys: List<PasskeyEntity> = Json.decodeFromString(content)
            passkeys
        }
    }

    private fun getStorageFile(): File {
        val file = File(context.filesDir, "passkeys.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        return file
    }

    @Serializable
    data class PasskeyEntity(
        val content: ByteArray,
        val domain: String,
        val title: String,
        val id: String
    )
}
