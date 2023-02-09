package proton.android.pass.presentation.utils

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.encrypt
import org.junit.Test
import proton.android.pass.commonui.api.ItemUiFilter
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.presentation.components.model.TestItemUiModel
import proton.android.pass.test.TestUtils.randomString
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.pass.domain.ItemType

internal class ItemFilterImplTest {

    @Test
    fun `empty query returns whole list`() {
        val itemList = listOf(TestItemUiModel.create(), TestItemUiModel.create())
        val filteredItems = ItemUiFilter.filterByQuery(itemList, "")
        assertThat(filteredItems).isEqualTo(itemList)
    }

    @Test
    fun `blank query returns empty list`() {
        val itemList = listOf(TestItemUiModel.create(), TestItemUiModel.create())
        val filteredItems = ItemUiFilter.filterByQuery(itemList, " ")
        assertThat(filteredItems).isEqualTo(emptyList<ItemUiModel>())
    }

    @Test
    fun `query that matches items returns the matching items`() {
        val title = randomString()
        val item1 = TestItemUiModel.create(title = title)
        val item2 = TestItemUiModel.create(title = "$title-abc")

        val filteredItems = ItemUiFilter.filterByQuery(
            listOf(item1, item2),
            title
        )

        assertThat(filteredItems.size).isEqualTo(2)
        assertThat(filteredItems[0]).isEqualTo(item1)
        assertThat(filteredItems[1]).isEqualTo(item2)

        val filteredItems2 = ItemUiFilter.filterByQuery(
            listOf(item1, item2),
            "$title-"
        )

        assertThat(filteredItems2.size).isEqualTo(1)
        assertThat(filteredItems2[0]).isEqualTo(item2)
    }

    @Test
    fun `search is case insensitive`() {
        val item1 = TestItemUiModel.create(title = "ABc")
        val item2 = TestItemUiModel.create(title = "aBc")
        val filteredItems = ItemUiFilter.filterByQuery(
            listOf(item1, item2),
            "ab"
        )

        assertThat(filteredItems.size).isEqualTo(2)
        assertThat(filteredItems[0]).isEqualTo(item1)
        assertThat(filteredItems[1]).isEqualTo(item2)
    }

    @Test
    fun `login item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = title,
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = emptyList(),
                    packageNames = emptyList(),
                    primaryTotp = randomString()
                )
            ),
            query = title
        )
    }

    @Test
    fun `login item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                note = note,
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = emptyList(),
                    packageNames = emptyList(),
                    primaryTotp = randomString()
                )
            ),
            query = note
        )
    }

    @Test
    fun `login item filtered by website`() {
        val website = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = listOf(website),
                    packageNames = emptyList(),
                    primaryTotp = randomString()
                )
            ),
            query = website
        )
    }

    @Test
    fun `note item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = title,
                itemType = ItemType.Note(
                    text = randomString()
                )
            ),
            query = title
        )
    }

    @Test
    fun `note item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = randomString(),
                note = randomString(),
                itemType = ItemType.Note(
                    text = note
                )
            ),
            query = note
        )
    }

    @Test
    fun `alias item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = title,
                note = randomString(),
                itemType = ItemType.Alias(
                    aliasEmail = randomString()
                )
            ),
            query = title
        )
    }

    @Test
    fun `alias item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = randomString(),
                note = note,
                itemType = ItemType.Alias(
                    aliasEmail = randomString()
                )
            ),
            query = note
        )
    }

    @Test
    fun `alias item filtered by alias`() {
        val alias = randomString()
        runTestWithItemAndQuery(
            item = TestItemUiModel.create(
                title = randomString(),
                note = randomString(),
                itemType = ItemType.Alias(
                    aliasEmail = alias
                )
            ),
            query = alias
        )
    }

    private fun runTestWithItemAndQuery(item: ItemUiModel, query: String) {
        val filteredItems = ItemUiFilter.filterByQuery(
            listOf(item),
            query
        )
        assertThat(filteredItems.size).isEqualTo(1)
        assertThat(filteredItems[0]).isEqualTo(item)

        val filteredItems2 = ItemUiFilter.filterByQuery(
            listOf(item),
            "${query}abc"
        )
        assertThat(filteredItems2).isEmpty()
    }
}
