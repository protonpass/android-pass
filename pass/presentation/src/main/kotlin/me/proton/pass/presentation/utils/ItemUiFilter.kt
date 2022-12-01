package me.proton.pass.presentation.utils

import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

object ItemUiFilter {
    fun filterByQuery(
        list: List<ItemUiModel>,
        query: String
    ): List<ItemUiModel> =
        if (query.isNotEmpty()) {
            val lowercaseQuery = query.lowercase()
            list.filter { it.matchesQuery(lowercaseQuery) }
        } else {
            list
        }

    private fun isItemMatch(item: ItemUiModel, query: String): Boolean {
        if (item.name.lowercase().contains(query)) return true
        if (item.note.lowercase().contains(query)) return true

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

    private fun ItemUiModel.matchesQuery(query: String): Boolean =
        isItemMatch(this, query)
}
