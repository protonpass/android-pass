/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.entities.AutofillFieldId
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.DatasetMapping
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.ui.autofill.ItemFieldMapper
import proton.android.pass.crypto.fakes.context.TestEncryptionContext

class ItemFieldMapperTest {

    @Test
    fun `can handle empty list`() {
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = autofillItem(),
            androidAutofillFieldIds = emptyList(),
            autofillTypes = emptyList(),
            fieldIsFocusedList = emptyList(),
            parentIdList = emptyList()
        )
        assertThat(res.mappings).isEmpty()
    }

    @Test
    fun `can map username when focused`() {
        val itemAutofillId = TestAutofillId(123)
        val item = autofillItem()
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(itemAutofillId),
            autofillTypes = listOf(FieldType.Username),
            fieldIsFocusedList = listOf(true),
            parentIdList = listOf(null)
        )

        val expected = DatasetMapping(
            autofillFieldId = itemAutofillId,
            contents = item.username,
            displayValue = item.username
        )
        assertThat(res.mappings).isEqualTo(listOf(expected))
    }

    @Test
    fun `can map username when not focused`() {
        val itemAutofillId = TestAutofillId(123)
        val item = autofillItem()
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(itemAutofillId),
            autofillTypes = listOf(FieldType.Username),
            fieldIsFocusedList = listOf(false),
            parentIdList = listOf(null)
        )

        val expected = DatasetMapping(
            autofillFieldId = itemAutofillId,
            contents = item.username,
            displayValue = item.username
        )
        assertThat(res.mappings).isEqualTo(listOf(expected))
    }

    @Test
    fun `maps email as username`() {
        val itemAutofillId = TestAutofillId(123)
        val item = autofillItem()
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(itemAutofillId),
            autofillTypes = listOf(FieldType.Email),
            fieldIsFocusedList = listOf(false),
            parentIdList = listOf(null)
        )

        val expected = DatasetMapping(
            autofillFieldId = itemAutofillId,
            contents = item.username,
            displayValue = item.username
        )
        assertThat(res.mappings).isEqualTo(listOf(expected))
    }

    @Test
    fun `can map password when focused`() {
        val itemAutofillId = TestAutofillId(123)

        val password = "somepassword"
        val item = autofillItem(password = TestEncryptionContext.encrypt(password))
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(itemAutofillId),
            autofillTypes = listOf(FieldType.Password),
            fieldIsFocusedList = listOf(true),
            parentIdList = listOf(null)
        )

        val expected = DatasetMapping(
            autofillFieldId = itemAutofillId,
            contents = password,
            displayValue = "" // Password has no display value
        )
        assertThat(res.mappings).isEqualTo(listOf(expected))
    }

    @Test
    fun `can map password when not focused`() {
        val itemAutofillId = TestAutofillId(123)

        val password = "somepassword"
        val item = autofillItem(password = TestEncryptionContext.encrypt(password))
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(itemAutofillId),
            autofillTypes = listOf(FieldType.Password),
            fieldIsFocusedList = listOf(false),
            parentIdList = listOf(null)
        )

        val expected = DatasetMapping(
            autofillFieldId = itemAutofillId,
            contents = password,
            displayValue = "" // Password has no display value
        )
        assertThat(res.mappings).isEqualTo(listOf(expected))
    }

    @Test
    fun `can map username and password`() {
        val usernameAutofillId = TestAutofillId(123)
        val passwordAutofillId = TestAutofillId(456)

        val username = "username"
        val password = "somepassword"
        val item = autofillItem(
            username = username,
            password = TestEncryptionContext.encrypt(password)
        )
        val res = ItemFieldMapper.mapFields(
            encryptionContext = TestEncryptionContext,
            autofillItem = item,
            androidAutofillFieldIds = listOf(usernameAutofillId, passwordAutofillId),
            autofillTypes = listOf(FieldType.Username, FieldType.Password),
            fieldIsFocusedList = listOf(false, false),
            parentIdList = listOf(null, null)
        )

        val expectedUsername = DatasetMapping(
            autofillFieldId = usernameAutofillId,
            contents = username,
            displayValue = username
        )
        val expectedPassword = DatasetMapping(
            autofillFieldId = passwordAutofillId,
            contents = password,
            displayValue = ""
        )
        assertThat(res.mappings).isEqualTo(listOf(expectedUsername, expectedPassword))
    }

    private fun autofillItem(
        itemId: String = "ItemID-123",
        shareId: String = "ShareId-123",
        username: String = "username",
        password: String = TestEncryptionContext.encrypt("password"),
    ) = AutofillItem(
        itemId = itemId,
        shareId = shareId,
        username = username,
        password = password,
        totp = "",
    )

    private data class TestAutofillId(val id: Int) : AutofillFieldId
}
