package proton.android.pass.autofill.select

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import org.junit.Test
import proton.android.pass.autofill.ui.autofill.select.SelectItemViewModel
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

class SelectItemViewModelTest {

    @Test
    fun `sortResults can handle empty results`() {
        val res = SelectItemViewModel.sortResults(emptyList(), emptyList(), None)
        assertThat(res.suggestions).isEmpty()
        assertThat(res.suggestionsForTitle).isEmpty()
        assertThat(res.items.get(GroupingKeys.NoGrouping)).isEmpty()
    }

    @Test
    fun `sortResults does not show duplicate items`() {
        val item1 = uiModel("item1")
        val item2 = uiModel("item2")
        val item3 = uiModel("item3")
        val suggestions = listOf(item1, item2)
        val allItems = listOf(item1, item2, item3)

        val res = SelectItemViewModel.sortResults(
            allItems = allItems,
            suggestions = suggestions,
            autofillAppState = None
        )
        assertThat(res.suggestions).isEqualTo(suggestions)
        assertThat(res.items.get(GroupingKeys.NoGrouping)).isEqualTo(listOf(item3))
    }

    @Test
    fun `sortResults sorts by time descending`() {
        val now = Clock.System.now()
        val item1 = uiModel("item1")
        val item2 = uiModel("item2", modificationTime = now.minus(10, DateTimeUnit.SECOND))
        val item3 = uiModel("item3", modificationTime = now)
        val suggestions = listOf(item1)
        val allItems = listOf(item1, item2, item3)

        val res = SelectItemViewModel.sortResults(
            allItems = allItems,
            suggestions = suggestions,
            autofillAppState = None
        )
        assertThat(res.suggestions).isEqualTo(suggestions)
        assertThat(res.items.get(GroupingKeys.NoGrouping)).isEqualTo(listOf(item3, item2))
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
