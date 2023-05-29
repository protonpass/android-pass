package proton.android.pass.commonui.api

import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemContents

object ItemUiFilter {
    fun filterByQuery(
        list: List<ItemUiModel>,
        query: String
    ): List<ItemUiModel> =
        if (query.isNotEmpty()) {
            if (query.isNotBlank()) {
                val lowercaseQuery = query.lowercase()
                list.filter { it.matchesQuery(lowercaseQuery) }
            } else {
                emptyList()
            }
        } else {
            list
        }

    private fun isItemMatch(item: ItemUiModel, query: String): Boolean {
        if (item.contents.title.lowercase().contains(query)) return true
        if (item.contents.note.lowercase().contains(query)) return true

        return when (val contents = item.contents) {
            is ItemContents.Alias -> isAliasMatch(contents, query)
            is ItemContents.Login -> isLoginMatch(contents, query)
            is ItemContents.Note -> isNoteMatch(contents, query)
            is ItemContents.Unknown -> return false
        }
    }

    private fun isAliasMatch(content: ItemContents.Alias, query: String): Boolean =
        content.aliasEmail.lowercase().contains(query)

    private fun isLoginMatch(content: ItemContents.Login, query: String): Boolean {
        if (content.username.lowercase().contains(query)) return true

        val anyWebsiteMatches = content.urls.any { it.lowercase().contains(query) }
        if (anyWebsiteMatches) return true

        return false
    }

    private fun isNoteMatch(content: ItemContents.Note, query: String): Boolean =
        content.note.lowercase().contains(query)

    private fun ItemUiModel.matchesQuery(query: String): Boolean =
        isItemMatch(this, query)
}
