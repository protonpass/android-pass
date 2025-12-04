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

package proton.android.pass.data.impl.migration.itemhastotp

import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.data.impl.extensions.hasTotp
import proton.android.pass.data.impl.extensions.toDomain
import proton.android.pass.data.impl.local.LocalItemDataSource
import proton.android.pass.data.impl.migration.Migrator
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

interface ItemHasTotpMigrator : Migrator {
    override val migrationName: String
        get() = "item_has_totp"
}

@Singleton
class ItemHasTotpMigratorImpl @Inject constructor(
    private val localItemDataSource: LocalItemDataSource,
    private val encryptionContextProvider: EncryptionContextProvider
) : ItemHasTotpMigrator {

    override suspend fun migrate() = safeRunCatching {
        val items = localItemDataSource.getItemsPendingForTotpMigration()
        if (items.isNotEmpty()) migrateItems(items)
    }.fold(
        onSuccess = {
            PassLogger.i(TAG, "Successfully finished migrating items")
        },
        onFailure = {
            PassLogger.e(TAG, it, "Failed to migrate items")
        }
    )

    private suspend fun migrateItems(items: List<ItemEntity>) {
        val updated = encryptionContextProvider.withEncryptionContext {
            items.map { entity ->
                val asItem = entity.toDomain(this@withEncryptionContext)
                val hasTotp = asItem.hasTotp(this@withEncryptionContext)
                entity.copy(hasTotp = hasTotp)
            }
        }

        localItemDataSource.upsertItems(updated)
        PassLogger.i(TAG, "Migrated ${updated.size} items")
    }

    companion object {
        private const val TAG = "ItemHasTotpMigratorImpl"
    }

}
