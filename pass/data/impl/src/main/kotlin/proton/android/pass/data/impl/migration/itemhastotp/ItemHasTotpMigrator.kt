package proton.android.pass.data.impl.migration.itemhastotp

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

    override suspend fun migrate() = runCatching {
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
