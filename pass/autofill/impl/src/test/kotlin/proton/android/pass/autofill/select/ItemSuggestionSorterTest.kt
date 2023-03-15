package proton.android.pass.autofill.select

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import org.junit.Test
import proton.android.pass.autofill.ui.autofill.select.ItemSuggestionSorter
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class ItemSuggestionSorterTest {

    @Test
    fun `sortResults can handle empty results`() {
        val res = ItemSuggestionSorter.sort(emptyList(), emptyList())
        assertThat(res.suggestions).isEmpty()
        assertThat(res.allItems).isEmpty()
    }

    @Test
    fun `sortResults does not show duplicate items`() {
        val now = Clock.System.now()
        val item1 = uiModel("item1", now)
        val item2 = uiModel("item2", now)
        val item3 = uiModel("item3", now)
        val suggestions = listOf(item1, item2)
        val allItems = listOf(item1, item2, item3)

        val res = ItemSuggestionSorter.sort(
            allItems = allItems,
            suggestions = suggestions
        )
        assertThat(res.suggestions).isEqualTo(suggestions)
        assertThat(res.allItems).isEqualTo(listOf(item3))
    }

    @Test
    fun `sortResults sorts by time descending`() {
        val now = Clock.System.now()
        val item1 = uiModel("item1")
        val item2 = uiModel("item2", modificationTime = now.minus(10, DateTimeUnit.SECOND))
        val item3 = uiModel("item3", modificationTime = now)
        val suggestions = listOf(item1)
        val allItems = listOf(item1, item2, item3)

        val res = ItemSuggestionSorter.sort(
            allItems = allItems,
            suggestions = suggestions,
        )
        assertThat(res.suggestions).isEqualTo(suggestions)
        assertThat(res.allItems).isEqualTo(listOf(item3, item2))
    }


    private fun uiModel(
        id: String,
        modificationTime: Instant = Clock.System.now(),
        lastAutofillTime: Instant? = null
    ): ItemUiModel = ItemUiModel(
        id = ItemId(id),
        shareId = ShareId("shareid"),
        name = "",
        note = "",
        itemType = ItemType.Password,
        createTime = Clock.System.now(),
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime
    )
}
