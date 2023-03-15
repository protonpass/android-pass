package proton.android.pass.autofill.ui.autofill.select

import kotlinx.datetime.Instant
import proton.android.pass.commonuimodels.api.ItemUiModel

data class ItemSuggestionResult(
    val allItems: List<ItemUiModel>,
    val suggestions: List<ItemUiModel>
)

object ItemSuggestionSorter {

    fun sort(allItems: List<ItemUiModel>, suggestions: List<ItemUiModel>): ItemSuggestionResult {
        val suggestionIds = suggestions.map { it.id }.toSet()
        val allItemsWithoutSuggestedAndSorted = allItems.filter { !suggestionIds.contains(it.id) }
            .sortedByDescending {
                recentDate(it.modificationTime, it.lastAutofillTime)
            }
        val suggestionsSorted = suggestions.sortedByDescending {
            recentDate(it.modificationTime, it.lastAutofillTime)
        }

        return ItemSuggestionResult(
            allItems = allItemsWithoutSuggestedAndSorted,
            suggestions = suggestionsSorted
        )
    }

    private fun recentDate(modificationTime: Instant, lastAutofillTime: Instant?): Instant =
        lastAutofillTime?.let { maxOf(it, modificationTime) } ?: modificationTime
}
