package me.proton.pass.search

import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import javax.inject.Inject

class ItemFilterImpl @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto
) : ItemFilter {

    override fun filterByQuery(itemsResult: Result<List<Item>>, query: String): Result<List<Item>> =
        if (query.isNotEmpty()) {
            val lowercaseQuery = query.lowercase()
            itemsResult.map { list -> list.filter { it.matchesQuery(lowercaseQuery) } }
        } else {
            itemsResult
        }

    private fun isItemMatch(item: Item, query: String): Boolean {
        val decryptedTitle = item.title.decrypt(keyStoreCrypto)
        if (decryptedTitle.lowercase().contains(query)) return true

        val decryptedNote = item.note.decrypt(keyStoreCrypto)
        if (decryptedNote.lowercase().contains(query)) return true

        return when (val itemType = item.itemType) {
            is ItemType.Alias -> isAliasMatch(itemType, query)
            is ItemType.Login -> isLoginMatch(itemType, query)
            is ItemType.Note -> isNoteMatch(itemType, query)
            is ItemType.Password -> false
        }
    }

    private fun isAliasMatch(itemType: ItemType.Alias, query: String): Boolean =
        itemType.aliasEmail.lowercase().contains(query)

    private fun isLoginMatch(itemType: ItemType.Login, query: String): Boolean {
        if (itemType.username.lowercase().contains(query)) return true

        val anyWebsiteMatches = itemType.websites.any { it.lowercase().contains(query) }
        if (anyWebsiteMatches) return true

        return false
    }

    private fun isNoteMatch(itemType: ItemType.Note, query: String): Boolean =
        itemType.text.lowercase().contains(query)

    private fun Item.matchesQuery(query: String): Boolean =
        isItemMatch(this, query)
}
