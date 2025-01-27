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

package proton.android.pass.features.itemcreate.alias.mailboxes

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.SelectedAliasMailboxUiModel
import proton.android.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.features.itemcreate.alias.mailboxes.presentation.SelectMailboxesUiState
import proton.android.pass.features.itemcreate.alias.mailboxes.presentation.SelectMailboxesViewModel

class SelectMailboxesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SelectMailboxesViewModel

    @Before
    fun setup() {
        viewModel = SelectMailboxesViewModel()
    }

    @Test
    fun `emits correct initial state`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(SelectMailboxesUiState.Initial)
        }
    }

    @Test
    fun `no mailboxes selected has button disabled`() = runTest {
        viewModel.setMailboxes(
            listOf(
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(id = 1, "")
                )
            )
        )

        viewModel.uiState.test {
            assertThat(awaitItem().canApply).isEqualTo(IsButtonEnabled.Disabled)
        }
    }

    @Test
    fun `at least one mailbox selected has button enabled`() = runTest {
        viewModel.setMailboxes(
            listOf(
                SelectedAliasMailboxUiModel(
                    selected = true,
                    model = AliasMailboxUiModel(id = 1, "")
                ),
                SelectedAliasMailboxUiModel(
                    selected = false,
                    model = AliasMailboxUiModel(id = 2, "")
                )
            )
        )

        viewModel.uiState.test {
            assertThat(awaitItem().canApply).isEqualTo(IsButtonEnabled.Enabled)
        }
    }

    @Test
    fun `onMailboxChange toggles selected`() = runTest {
        val mailbox1 = SelectedAliasMailboxUiModel(
            selected = false,
            model = AliasMailboxUiModel(id = 1, "")
        )

        val mailbox2 = SelectedAliasMailboxUiModel(
            selected = false,
            model = AliasMailboxUiModel(id = 2, "")
        )

        viewModel.setMailboxes(listOf(mailbox1, mailbox2))
        viewModel.onMailboxChanged(mailbox1)
        viewModel.uiState.test {
            val item = awaitItem()

            assertThat(item.mailboxes).isEqualTo(
                listOf(
                    mailbox1.copy(selected = true),
                    mailbox2
                )
            )
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onMailboxChanged(mailbox2)
        viewModel.uiState.test {
            val item = awaitItem()

            assertThat(item.mailboxes).isEqualTo(
                listOf(
                    mailbox1.copy(selected = true),
                    mailbox2.copy(selected = true)
                )
            )
        }
    }

}
