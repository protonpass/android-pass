/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.commonrust.impl.ItemTestData.concealed
import proton.android.pass.commonrust.impl.ItemTestData.encryptionContext
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.types.ItemExtraField as LibraryItemExtraField
import proton.android.pass.types.ItemExtraFieldContent as LibraryItemExtraFieldContent

@RunWith(TestParameterInjector::class)
internal class ItemFieldContentMappersTest {

    @Test
    internal fun `WHEN mapping date to library THEN writes seconds`(@TestParameter case: DateToLibraryCase) {
        val libraryContent = toLibraryFieldContent(CustomFieldContent.Date(label = LABEL, value = case.millis))

        assertThat(libraryContent).isEqualTo(LibraryItemExtraFieldContent.Timestamp(case.expectedSeconds))
    }

    @Test
    internal fun `WHEN mapping timestamp from library THEN returns millis`(@TestParameter case: DateFromLibraryCase) {
        val field = LibraryItemExtraField(
            name = LABEL,
            content = LibraryItemExtraFieldContent.Timestamp(case.seconds)
        ).toDomain(encryptionContext)

        assertThat(field).isEqualTo(CustomField.Date(label = LABEL, value = case.expectedMillis))
    }

    @Test
    internal fun `WHEN round-tripping date THEN keeps whole seconds`(@TestParameter case: DateToLibraryCase) {
        val field = roundTrip(CustomFieldContent.Date(label = LABEL, value = case.millis))

        val expectedMillis = case.expectedSeconds.takeIf { it != 0L }?.let { it * MILLIS_IN_SECOND }
        assertThat(field).isEqualTo(CustomField.Date(label = LABEL, value = expectedMillis))
    }

    @Test
    internal fun `WHEN round-tripping text field THEN keeps value`(@TestParameter case: TextCase) {
        val field = roundTrip(CustomFieldContent.Text(label = LABEL, value = case.value))

        assertThat(field).isEqualTo(CustomField.Text(label = LABEL, value = case.value))
    }

    @Test
    internal fun `WHEN round-tripping hidden field THEN keeps clear value`(@TestParameter case: TextCase) {
        val field = roundTrip(CustomFieldContent.Hidden(label = LABEL, value = concealed(case.value)))

        assertThat(field).isEqualTo(CustomField.Hidden(label = LABEL, value = encryptionContext.encrypt(case.value)))
    }

    @Test
    internal fun `WHEN round-tripping totp field THEN keeps clear value`(@TestParameter case: TextCase) {
        val field = roundTrip(CustomFieldContent.Totp(label = LABEL, value = concealed(case.value)))

        assertThat(field).isEqualTo(CustomField.Totp(label = LABEL, value = encryptionContext.encrypt(case.value)))
    }

    internal enum class DateToLibraryCase(val millis: Long?, val expectedSeconds: Long) {
        Empty(millis = null, expectedSeconds = 0L),
        Epoch(millis = 0L, expectedSeconds = 0L),
        MidnightUtc(millis = 1_699_920_000_000L, expectedSeconds = 1_699_920_000L),
        SubSecondTruncated(millis = 1_699_920_000_999L, expectedSeconds = 1_699_920_000L),
        BeforeEpoch(millis = -86_400_000L, expectedSeconds = -86_400L),
        FarFuture(millis = 253_402_214_400_000L, expectedSeconds = 253_402_214_400L)
    }

    internal enum class DateFromLibraryCase(val seconds: Long, val expectedMillis: Long?) {
        Unset(seconds = 0L, expectedMillis = null),
        OneSecond(seconds = 1L, expectedMillis = 1_000L),
        MidnightUtc(seconds = 1_699_920_000L, expectedMillis = 1_699_920_000_000L),
        BeforeEpoch(seconds = -86_400L, expectedMillis = -86_400_000L),
        FarFuture(seconds = 253_402_214_400L, expectedMillis = 253_402_214_400_000L)
    }

    internal enum class TextCase(val value: String) {
        Empty(""),
        Plain("value"),
        Unicode("contraseña 🔑"),
        Multiline("line1\nline2")
    }

    private fun toLibraryFieldContent(field: CustomFieldContent): LibraryItemExtraFieldContent = contentsWith(field)
        .toLibraryItemData(itemUuid = "uuid", existing = null, encryptionContext = encryptionContext)
        .extraFields
        .single()
        .content

    private fun roundTrip(field: CustomFieldContent): CustomField = contentsWith(field)
        .toLibraryItemData(itemUuid = "uuid", existing = null, encryptionContext = encryptionContext)
        .toItemType(encryptionContext, aliasEmail = null)
        .let { (it as ItemType.Note).customFields.single() }

    private fun contentsWith(field: CustomFieldContent) = ItemContents.Note(
        title = "title",
        note = "note",
        customFields = listOf(field)
    )

    private companion object {
        const val LABEL = "field"
    }
}
