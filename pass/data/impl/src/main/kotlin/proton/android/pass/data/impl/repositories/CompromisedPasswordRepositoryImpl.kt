/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.CompromisedPasswordItem
import proton.android.pass.data.api.repositories.CompromisedPasswordRepository
import proton.android.pass.data.impl.db.entities.CompromisedPasswordEntity
import proton.android.pass.data.impl.local.LocalCompromisedPasswordDataSource
import proton.android.pass.data.impl.remote.PrefixQueryResult
import proton.android.pass.data.impl.remote.RemoteCompromisedPasswordDataSource
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.isCheckSkipped
import proton.android.pass.log.api.PassLogger
import java.security.MessageDigest
import javax.inject.Inject

class CompromisedPasswordRepositoryImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val remoteDataSource: RemoteCompromisedPasswordDataSource,
    private val localDataSource: LocalCompromisedPasswordDataSource
) : CompromisedPasswordRepository {

    override fun observeCompromisedItems(userId: UserId): Flow<List<CompromisedPasswordItem>> =
        localDataSource.observeCompromisedItems(userId).map { entities ->
            entities.map { entity ->
                CompromisedPasswordItem(
                    shareId = ShareId(entity.shareId),
                    itemId = ItemId(entity.itemId)
                )
            }
        }

    override fun observeIsItemCompromised(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Boolean> = localDataSource.observeIsCompromised(userId, shareId, itemId)

    override suspend fun refresh(userId: UserId, items: List<Item>) {
        val hashToItems = buildHashToItemsMap(items)
        if (hashToItems.isEmpty()) return

        val existingByItemKey = localDataSource.getAllCheckedItems(userId)
            .associateBy { it.shareId to it.itemId }

        val dbUpdatedAtMs = remoteDataSource.getLastChange()?.let { it * SECONDS_TO_MS }

        val plan = classify(hashToItems, existingByItemKey, dbUpdatedAtMs)
        if (plan.isEmpty()) return

        val prefixCache = mutableMapOf<Pair<String, String?>, PrefixQueryResult>()
        val now = System.currentTimeMillis()
        val toPersist = mutableListOf<CompromisedPasswordEntity>()

        for (entry in plan) {
            val result = prefixCache.getOrPut(entry.prefix to entry.etagToSend) {
                remoteDataSource.getCompromisedSuffixes(entry.prefix, entry.etagToSend)
            }
            toPersist += applyResult(entry, result, existingByItemKey, userId, now)
        }

        if (toPersist.isNotEmpty()) {
            try {
                localDataSource.upsertAll(toPersist)
            } catch (e: android.database.sqlite.SQLiteException) {
                PassLogger.w(TAG, "Failed to persist compromised password results")
                PassLogger.w(TAG, e)
            }
        }
    }

    private fun buildHashToItemsMap(items: List<Item>): Map<String, MutableList<Item>> {
        val hashToItems = mutableMapOf<String, MutableList<Item>>()
        encryptionContextProvider.withEncryptionContext {
            items.forEach { item ->
                if (item.hasSkippedHealthCheck ||
                    item.isCheckSkipped(ItemFlag.SkipCompromisedPasswordCheck)
                ) return@forEach
                when (val itemType = item.itemType) {
                    is ItemType.Login -> {
                        val password = decrypt(itemType.password)
                        if (password.isNotBlank()) {
                            val hash = sha1Hex(password)
                            hashToItems.getOrPut(hash) { mutableListOf() }.add(item)
                        }
                    }
                    else -> {}
                }
            }
        }
        return hashToItems
    }

    private data class PlanEntry(
        val item: Item,
        val hash: String,
        val prefix: String,
        val etagToSend: String?
    )

    private fun classify(
        hashToItems: Map<String, List<Item>>,
        existingByItemKey: Map<Pair<String, String>, CompromisedPasswordEntity>,
        dbUpdatedAtMs: Long?
    ): List<PlanEntry> {
        val entries = mutableListOf<PlanEntry>()
        for ((hash, items) in hashToItems) {
            val prefix = hash.substring(0, PREFIX_LENGTH)
            for (item in items) {
                val existing = existingByItemKey[item.shareId.id to item.id.id]
                when {
                    existing == null ->
                        entries += PlanEntry(item, hash, prefix, etagToSend = null)
                    existing.passwordHash != prefix ->
                        entries += PlanEntry(item, hash, prefix, etagToSend = null)
                    dbUpdatedAtMs == null -> {
                        // last_change fetch failed — keep existing row, skip.
                    }
                    dbUpdatedAtMs <= existing.checkedAt -> {
                        // Corpus has not changed since last check — skip.
                    }
                    else ->
                        entries += PlanEntry(item, hash, prefix, etagToSend = existing.lastEtag)
                }
            }
        }
        return entries
    }

    private fun applyResult(
        entry: PlanEntry,
        result: PrefixQueryResult,
        existingByItemKey: Map<Pair<String, String>, CompromisedPasswordEntity>,
        userId: UserId,
        now: Long
    ): List<CompromisedPasswordEntity> = when (result) {
        is PrefixQueryResult.Ok -> listOf(
            CompromisedPasswordEntity(
                userId = userId.id,
                shareId = entry.item.shareId.id,
                itemId = entry.item.id.id,
                isCompromised = entry.hash.substring(PREFIX_LENGTH) in result.suffixes,
                passwordHash = entry.prefix,
                checkedAt = now,
                lastEtag = result.etag
            )
        )
        PrefixQueryResult.NotModified -> {
            val existing = existingByItemKey[entry.item.shareId.id to entry.item.id.id]
            if (existing == null) emptyList() else listOf(existing.copy(checkedAt = now))
        }
        PrefixQueryResult.Error -> {
            PassLogger.w(TAG, "Failed to check compromised passwords for a prefix")
            emptyList()
        }
    }

    private fun sha1Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02X".format(it) }
    }

    companion object {
        private const val TAG = "CompromisedPasswordRepositoryImpl"
        private const val PREFIX_LENGTH = 6
        private const val SECONDS_TO_MS = 1_000L
    }
}
