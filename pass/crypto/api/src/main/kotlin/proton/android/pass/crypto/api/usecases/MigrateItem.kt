/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.crypto.api.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.domain.key.ShareKey

data class EncryptedMigrateItemBody(
    val item: EncryptedMigrateContent,
    val history: List<EncryptedMigrateItemHistory>
)

data class EncryptedMigrateContent(
    val keyRotation: Long,
    val contentFormatVersion: Int,
    val content: String,
    val itemKey: String
)

data class EncryptedMigrateItemHistory(
    val revision: Long,
    val content: EncryptedMigrateContent
)

interface MigrateItem {

    fun migrate(destinationKey: ShareKey, payload: ItemMigrationPayload): EncryptedMigrateItemBody

}

data class ItemMigrationPayload(
    val itemContent: ItemMigrationContent,
    val historyContents: List<ItemMigrationHistoryContent>
)

data class ItemMigrationContent(
    val encryptedItemContents: EncryptedByteArray,
    val contentFormatVersion: Int
)

data class ItemMigrationHistoryContent(
    val revision: Long,
    val itemContent: ItemMigrationContent
)
