package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel
import proton.android.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectMailboxesDialogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SelectMailboxesDialogViewModel

    @Before
    fun setup() {
        viewModel = SelectMailboxesDialogViewModel()
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
