package me.proton.core.pass.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.pass.data.db.entities.SecretEntity

@Dao
abstract class SecretsDao : BaseDao<SecretEntity>() {
    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.ADDRESS_ID} = :addressId 
            AND ${SecretEntity.Columns.NAME} LIKE '%' || :query || '%'
        """
    )
    abstract suspend fun searchWithName(addressId: String, query: String): List<SecretEntity>
    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.NAME} LIKE '%' || :query || '%'
        """
    )
    abstract suspend fun searchWithName(query: String): List<SecretEntity>

    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.ADDRESS_ID} = :addressId 
            AND ${SecretEntity.Columns.ASSOCIATED_URIS} LIKE '%' || :uri || '%'
        """
    )
    abstract suspend fun searchWithUri(addressId: String, uri: String): List<SecretEntity>
    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.ASSOCIATED_URIS} LIKE '%' || :uri || '%'
        """
    )
    abstract suspend fun searchWithUri(uri: String): List<SecretEntity>

    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.ADDRESS_ID} = :addressId
        """
    )
    abstract fun observeAllForAddress(addressId: String): Flow<List<SecretEntity>>

    @Query(
        """
        SELECT * FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeAllForUser(userId: String): Flow<List<SecretEntity>>

    @Query(
        """
        DELETE FROM ${SecretEntity.TABLE} 
        WHERE ${SecretEntity.Columns.ID} = :secretId
        """
    )
    abstract suspend fun delete(secretId: String): Int
}
