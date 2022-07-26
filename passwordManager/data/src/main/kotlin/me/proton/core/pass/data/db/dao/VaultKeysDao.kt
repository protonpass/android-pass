package me.proton.core.pass.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.pass.data.db.entities.VaultKeyEntity

@Dao
abstract class VaultKeysDao : BaseDao<VaultKeyEntity>() {
    @Query(
        """
        SELECT * FROM ${VaultKeyEntity.TABLE}
        WHERE ${VaultKeyEntity.Columns.USER_ID} = :userId
          AND ${VaultKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract suspend fun getAllForShare(userId: String, shareId: String): List<VaultKeyEntity>

    @Query(
        """
        SELECT * FROM ${VaultKeyEntity.TABLE}
        WHERE ${VaultKeyEntity.Columns.USER_ID} = :userId
          AND ${VaultKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${VaultKeyEntity.Columns.ID} = :keyId
        LIMIT 1
        """
    )
    abstract suspend fun getById(userId: String, shareId: String, keyId: String): VaultKeyEntity?
}
