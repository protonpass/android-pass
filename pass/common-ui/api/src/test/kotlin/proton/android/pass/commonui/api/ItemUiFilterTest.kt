package proton.android.pass.commonui.api
/*
 * Copyright (c) 2023-2026 Proton AG
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

import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import proton.android.pass.commonui.api.ItemUiFilter.filterByQuery
import proton.android.pass.commonuimodels.fakes.TestItemUiModel
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemId

internal class ItemUiFilterTest {

    @Test
    fun `filterByQuery should return the same list when query is empty`() {
        val list = listOf(
            TestItemUiModel.create(title = "item1", note = "note1"),
            TestItemUiModel.create(title = "item2", note = "note2")
        )
        val query = ""

        val result = list.filterByQuery(query)

        assertEquals(list, result)
    }

    @Test
    fun `filterByQuery should return an empty list when query is blank`() {
        val list = listOf(
            TestItemUiModel.create(title = "item1", note = "note1"),
            TestItemUiModel.create(title = "item2", note = "note2")
        )
        val query = "   "

        val result = list.filterByQuery(query)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterByQuery should filter items based on the query`() {
        val list = listOf(
            TestItemUiModel.create(title = "item1", note = "note1"),
            TestItemUiModel.create(title = "item2", note = "note2")
        )
        val query = "item1"

        val result = list.filterByQuery(query)

        assertEquals(1, result.size)
        assertEquals("item1", result[0].contents.title)
        assertEquals("note1", result[0].contents.note)
    }

    @Test
    fun `filterByQuery should match alias title`() {
        val itemList = createAliasList()
        val filteredList = itemList.filterByQuery("title")

        assertEquals(2, filteredList.size)
    }

    @Test
    fun `filterByQuery should match alias email`() {
        val itemList = createAliasList()
        val filteredList = itemList.filterByQuery("test@example.com")

        assertEquals(1, filteredList.size)
    }

    @Test
    fun `GIVEN email query WHEN filterByQuery THEN should match login items with containing email`() {
        val query = "user@"
        val itemList = createLoginList()
        val expectedLoginItemsCount = 2

        val filteredList = itemList.filterByQuery(query)

        assertEquals(expectedLoginItemsCount, filteredList.size)
    }

    @Test
    fun `GIVEN username query WHEN filterByQuery THEN should match login items with containing username`() {
        val query = "usern"
        val itemList = createLoginList()
        val expectedLoginItemsCount = 2

        val filteredList = itemList.filterByQuery(query)

        assertEquals(expectedLoginItemsCount, filteredList.size)
    }

    @Test
    fun `filterByQuery should match URL`() {
        val itemList = createLoginList()
        val filteredList = itemList.filterByQuery("test")

        assertEquals(1, filteredList.size)
    }

    @Test
    fun `filterByQuery should match cc title`() {
        val itemList = createCreditCardList()
        val filteredList = itemList.filterByQuery("cc")

        assertEquals(1, filteredList.size)
    }

    @Test
    fun `filterByQuery should match cc cardholder`() {
        val itemList = createCreditCardList()
        val filteredList = itemList.filterByQuery("maecenas")

        assertEquals(1, filteredList.size)
    }

    @Test
    fun `filterByQuery should match cc note`() {
        val itemList = createCreditCardList()
        val filteredList = itemList.filterByQuery("pertinacia")

        assertEquals(1, filteredList.size)
    }

    @Test
    fun `filterByQuery should restrict matches when containing spaces`() {
        val selectedId = "selectedId"
        val items = listOf(
            TestItemUiModel.create(
                id = selectedId,
                title = "Tablet",
                note = "Tablet note",
                itemContents = ItemContents.Login(
                    title = "tablet",
                    note = "Tablet note",
                    itemEmail = "user@email.com",
                    itemUsername = "username",
                    password = HiddenState.Concealed(""),
                    urls = listOf("exampleurl.test", "otherurl.test"),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Concealed(""),
                    customFields = emptyList(),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            ),
            TestItemUiModel.create(
                id = "other",
                title = "Tablet",
                note = "Tablet second note",
                itemContents = ItemContents.Login(
                    title = "tablet",
                    note = "Tablet note",
                    itemEmail = "user@email.com",
                    itemUsername = "username",
                    password = HiddenState.Concealed(""),
                    urls = listOf("randomurl.test", "testurl.test"),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Concealed(""),
                    customFields = emptyList(),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            ),
            TestItemUiModel.create(
                id = "phone",
                title = "Phone",
                note = "Phone note",
                itemContents = ItemContents.Login(
                    title = "phone",
                    note = "Phone note",
                    itemEmail = "user@email.com",
                    itemUsername = "username",
                    password = HiddenState.Concealed(""),
                    urls = listOf("exampleurl.test", "testurl.test"),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Concealed(""),
                    customFields = emptyList(),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            )
        )

        val filteredList = items.filterByQuery("tablet    example ")
        assertThat(filteredList.size).isEqualTo(1)
        assertThat(filteredList.first().id).isEqualTo(ItemId(selectedId))
    }

    @Test
    fun `filterByQuery should take login item custom fields of type text into account`() {
        val items = listOf(
            TestItemUiModel.create(
                id = "itemId",
                title = "Tablet",
                note = "Tablet note",
                itemContents = ItemContents.Login(
                    title = "tablet",
                    note = "Tablet note",
                    itemEmail = "user@email.com",
                    itemUsername = "username",
                    password = HiddenState.Concealed(""),
                    urls = listOf("exampleurl.test", "otherurl.test"),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Concealed(""),
                    customFields = listOf(
                        CustomFieldContent.Text(label = "label", value = "value"),
                        CustomFieldContent.Hidden(label = "hidden", value = HiddenState.Empty("")),
                        CustomFieldContent.Totp(label = "totp", value = HiddenState.Empty(""))

                    ),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            )
        )

        // By value
        val filteredValueList = items.filterByQuery("value")
        assertThat(filteredValueList.size).isEqualTo(1)

        // By label
        val filteredLabelList = items.filterByQuery("label")
        assertThat(filteredLabelList.size).isEqualTo(1)

        // Can find by Hidden field label
        val filteredHiddenList = items.filterByQuery("hidden")
        assertThat(filteredHiddenList.size).isEqualTo(1)

        // Can find by Totp field label
        val filteredTotpList = items.filterByQuery("totp")
        assertThat(filteredTotpList.size).isEqualTo(1)

    }

    @Test
    fun `filterByQuery should match alias slNote`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Alias(
                    title = "Title",
                    note = "Note",
                    aliasEmail = "alias@example.com",
                    customFields = emptyList(),
                    slNote = "my important SimpleLogin note"
                )
            ),
            TestItemUiModel.create(
                itemContents = ItemContents.Alias(
                    title = "Other Title",
                    note = "Other Note",
                    aliasEmail = "other@example.com",
                    customFields = emptyList(),
                    slNote = ""
                )
            )
        )

        val filteredList = items.filterByQuery("SimpleLogin")
        assertThat(filteredList).hasSize(1)

        val filteredList2 = items.filterByQuery("important")
        assertThat(filteredList2).hasSize(1)
    }

    @Test
    fun `filterByQuery should match alias custom text fields`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Alias(
                    title = "Title",
                    note = "Note",
                    aliasEmail = "alias@example.com",
                    customFields = listOf(CustomFieldContent.Text(label = "nick", value = "coolalias"))
                )
            )
        )

        val byValue = items.filterByQuery("coolalias")
        assertThat(byValue).hasSize(1)

        val byLabel = items.filterByQuery("nick")
        assertThat(byLabel).hasSize(1)
    }

    @Test
    fun `filterByQuery should match credit card custom text fields`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.CreditCard(
                    title = "CC Item",
                    note = "Note",
                    cardHolder = "holder",
                    type = CreditCardType.MasterCard,
                    number = "1234",
                    cvv = HiddenState.Empty(""),
                    pin = HiddenState.Empty(""),
                    expirationDate = "12/25",
                    customFields = listOf(CustomFieldContent.Text(label = "nickname", value = "travel"))
                )
            )
        )

        val byValue = items.filterByQuery("travel")
        assertThat(byValue).hasSize(1)

        val byLabel = items.filterByQuery("nickname")
        assertThat(byLabel).hasSize(1)
    }

    @Test
    fun `filterByQuery should match note custom text fields`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Note(
                    title = "Note title",
                    note = "body",
                    customFields = listOf(CustomFieldContent.Text(label = "tag", value = "important"))
                )
            )
        )

        val byValue = items.filterByQuery("important")
        assertThat(byValue).hasSize(1)

        val byLabel = items.filterByQuery("tag")
        assertThat(byLabel).hasSize(1)
    }

    @Test
    fun `filterByQuery should match hidden custom field label`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Login(
                    title = "Login",
                    note = "",
                    itemEmail = "",
                    itemUsername = "",
                    password = HiddenState.Empty(""),
                    urls = emptyList(),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Empty(""),
                    customFields = listOf(
                        CustomFieldContent.Hidden(label = "mysecretlabel", value = HiddenState.Concealed("secretvalue"))
                    ),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            )
        )

        val byLabel = items.filterByQuery("mysecretlabel")
        assertThat(byLabel).hasSize(1)

        val byValue = items.filterByQuery("secretvalue")
        assertThat(byValue).hasSize(0)
    }

    @Test
    fun `filterByQuery should match totp custom field label`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Login(
                    title = "Login",
                    note = "",
                    itemEmail = "",
                    itemUsername = "",
                    password = HiddenState.Empty(""),
                    urls = emptyList(),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Empty(""),
                    customFields = listOf(
                        CustomFieldContent.Totp(label = "mytotplabel", value = HiddenState.Concealed("totpseed"))
                    ),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            )
        )

        val byLabel = items.filterByQuery("mytotplabel")
        assertThat(byLabel).hasSize(1)

        val byValue = items.filterByQuery("totpseed")
        assertThat(byValue).hasSize(0)
    }

    @Test
    fun `filterByQuery should match section title`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Custom(
                    title = "Custom Item",
                    note = "",
                    customFields = emptyList(),
                    sectionContentList = listOf(
                        ExtraSectionContent(title = "sectiontitle", customFieldList = emptyList())
                    )
                )
            )
        )

        val matched = items.filterByQuery("sectiontitle")
        assertThat(matched).hasSize(1)

        val noMatch = items.filterByQuery("other")
        assertThat(noMatch).hasSize(0)
    }

    @Test
    fun `filterByQuery should match section custom fields`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Custom(
                    title = "Custom Item",
                    note = "",
                    customFields = emptyList(),
                    sectionContentList = listOf(
                        ExtraSectionContent(
                            title = "section",
                            customFieldList = listOf(
                                CustomFieldContent.Text(label = "sectionlabel", value = "sectionvalue")
                            )
                        )
                    )
                )
            )
        )

        val byLabel = items.filterByQuery("sectionlabel")
        assertThat(byLabel).hasSize(1)

        val byValue = items.filterByQuery("sectionvalue")
        assertThat(byValue).hasSize(1)

        val noMatch = items.filterByQuery("other")
        assertThat(noMatch).hasSize(0)
    }

    @Test
    fun `filterByQuery should match date custom field label`() {
        val items = listOf(
            TestItemUiModel.create(
                itemContents = ItemContents.Login(
                    title = "Login",
                    note = "",
                    itemEmail = "",
                    itemUsername = "",
                    password = HiddenState.Empty(""),
                    urls = emptyList(),
                    packageInfoSet = setOf(),
                    primaryTotp = HiddenState.Empty(""),
                    customFields = listOf(
                        CustomFieldContent.Date(label = "mydatelabel", value = 1_700_000_000L)
                    ),
                    passkeys = emptyList(),
                    autofillUrls = emptyList()
                )
            )
        )

        val byLabel = items.filterByQuery("mydatelabel")
        assertThat(byLabel).hasSize(1)
    }

    private fun createAliasList() = listOf(
        TestItemUiModel.create(
            itemContents = ItemContents.Alias(
                title = "Title",
                note = "Note",
                aliasEmail = "alias@example.com",
                customFields = emptyList()
            )
        ),
        TestItemUiModel.create(
            itemContents = ItemContents.Alias(
                title = "Another Title",
                note = "Another Note",
                aliasEmail = "test@example.com",
                customFields = emptyList()
            )
        )
    )

    private fun createCreditCardList() = listOf(
        TestItemUiModel.create(
            itemContents = ItemContents.CreditCard(
                title = "CC Item",
                note = "Note",
                cardHolder = "maecenas",
                type = CreditCardType.MasterCard,
                number = "ancillae",
                cvv = HiddenState.Empty(""),
                pin = HiddenState.Empty(""),
                expirationDate = "pro",
                customFields = emptyList()
            )
        ),
        TestItemUiModel.create(
            itemContents = ItemContents.CreditCard(
                title = "dis",
                note = "pertinacia",
                cardHolder = "ei",
                type = CreditCardType.MasterCard,
                number = "viris",
                cvv = HiddenState.Empty(""),
                pin = HiddenState.Empty(""),
                expirationDate = "pertinacia",
                customFields = emptyList()
            )
        )
    )

    private fun createLoginList() = listOf(
        TestItemUiModel.create(
            itemContents = ItemContents.Login(
                title = "Login Item",
                note = "Note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                urls = listOf("example.com", "test.com"),
                password = HiddenState.Empty(""),
                packageInfoSet = setOf(),
                primaryTotp = HiddenState.Empty(""),
                customFields = listOf(),
                passkeys = emptyList(),
                autofillUrls = emptyList()
            )
        ),
        TestItemUiModel.create(
            itemContents = ItemContents.Login(
                title = "Another Login",
                note = "Note",
                itemEmail = "user@email.com",
                itemUsername = "username",
                urls = listOf("google.com"),
                password = HiddenState.Empty(""),
                packageInfoSet = setOf(),
                primaryTotp = HiddenState.Empty(""),
                customFields = listOf(),
                passkeys = emptyList(),
                autofillUrls = emptyList()
            )
        )
    )
}
