package me.proton.core.pass.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.usecases.ObserveActiveItems
import javax.inject.Inject

class SearchItemsImpl @Inject constructor(
    private val crypto: KeyStoreCrypto,
    observeActiveItems: ObserveActiveItems
) : SearchItems {

    private val queryState: MutableStateFlow<String> = MutableStateFlow("")
    private val resultsFlow: Flow<List<Item>> = combine(
        observeActiveItems(),
        queryState,
        ::filterItems
    )

    private fun filterItems(items: List<Item>, query: String): List<Item> =
        if (query.isNotEmpty()) {
            val lowercaseQuery = query.lowercase()
            items.filter { it.matchesQuery(lowercaseQuery) }
        } else {
            items
        }

    override fun observeResults(): Flow<List<Item>> = resultsFlow

    override fun updateQuery(query: String) {
        queryState.value = query
    }

    override fun clearSearch() {
        queryState.value = ""
    }

    private fun isItemMatch(item: Item, query: String): Boolean {
        val decryptedTitle = item.title.decrypt(crypto)
        if (decryptedTitle.lowercase().contains(query)) return true

        val decryptedNote = item.note.decrypt(crypto)
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
