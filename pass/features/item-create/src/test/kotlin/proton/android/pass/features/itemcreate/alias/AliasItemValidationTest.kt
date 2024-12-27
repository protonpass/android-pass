/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.alias

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonrust.api.AliasPrefixError
import proton.android.pass.commonrust.fakes.TestAliasPrefixValidator

class AliasItemValidationTest {

    @Test
    fun `empty title should return an error`() {
        val item = itemWithContents(title = "")

        val res = item.validate(allowEmptyTitle = false, aliasPrefixValidator = successValidator)
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankTitle)
    }

    @Test
    fun `empty title allowing empty title should be ok`() {
        val item = itemWithContents(title = "")

        val res = item.validate(allowEmptyTitle = true, aliasPrefixValidator = successValidator)
        assertThat(res).isEmpty()
    }

    @Test
    fun `empty alias should return an error`() {
        val item = itemWithContents(prefix = "")

        val res = item.validate(
            allowEmptyTitle = false,
            aliasPrefixValidator = errorValidator(AliasPrefixError.PrefixEmpty)
        )
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.BlankPrefix)
    }

    @Test
    fun `alias with invalid characters return an error`() {
        val item = itemWithContents(prefix = "abc!=()")

        val res = item.validate(
            allowEmptyTitle = false,
            aliasPrefixValidator = errorValidator(AliasPrefixError.InvalidCharacter)
        )
        assertThat(res.size).isEqualTo(1)
        assertThat(res.first()).isEqualTo(AliasItemValidationErrors.InvalidAliasContent)
    }

    private fun itemWithContents(
        title: String = "sometitle",
        prefix: String = "somealias",
        mailboxes: List<SelectedAliasMailboxUiModel>? = null
    ): AliasItemFormState {
        return AliasItemFormState(
            title = title,
            prefix = prefix,
            mailboxes = mailboxes ?: listOf(
                SelectedAliasMailboxUiModel(
                    AliasMailboxUiModel(
                        1,
                        "email"
                    ),
                    true
                )
            )
        )
    }

    companion object {
        private val successValidator = TestAliasPrefixValidator()
        private fun errorValidator(error: AliasPrefixError) = TestAliasPrefixValidator().apply {
            setResult(Result.failure(error))
        }
    }

}
