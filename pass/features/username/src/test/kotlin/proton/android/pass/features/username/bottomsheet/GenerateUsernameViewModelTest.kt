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

package proton.android.pass.features.username.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.commonrust.fakes.FakeUsernameGenerator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.usernames.FakeObserveUsernameConfig
import proton.android.pass.data.fakes.usecases.usernames.FakeUpdateUsernameConfig
import proton.android.pass.features.username.GenerateUsernameBottomsheetMode
import proton.android.pass.features.username.GenerateUsernameBottomsheetModeValue
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule

internal class GenerateUsernameViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var stateHandleProvider: SavedStateHandleProvider
    private lateinit var observeConfig: FakeObserveUsernameConfig
    private lateinit var updateConfig: FakeUpdateUsernameConfig
    private lateinit var generator: FakeUsernameGenerator
    private lateinit var draftRepository: FakeDraftRepository
    private lateinit var clipboard: FakeClipboardManager
    private lateinit var viewModel: GenerateUsernameViewModel

    @Before
    internal fun setUp() {
        stateHandleProvider = FakeSavedStateHandleProvider()
        stateHandleProvider.get()[GenerateUsernameBottomsheetMode.key] =
            GenerateUsernameBottomsheetModeValue.CopyAndClose.name

        observeConfig = FakeObserveUsernameConfig()
        updateConfig = FakeUpdateUsernameConfig()
        generator = FakeUsernameGenerator().apply { result = "happy-cat" }
        draftRepository = FakeDraftRepository()
        clipboard = FakeClipboardManager()

        viewModel = GenerateUsernameViewModel(
            stateHandleProvider = stateHandleProvider,
            observeUsernameConfig = observeConfig,
            usernameGenerator = generator,
            updateUsernameConfig = updateConfig,
            snackbarDispatcher = FakeSnackbarDispatcher(),
            clipboardManager = clipboard,
            draftRepository = draftRepository
        )
    }

    @Test
    internal fun `initial state generates a username from config`() = runTest {
        viewModel.stateFlow.test {
            val populated = awaitItem()
            assertThat(populated.username).isEqualTo("happy-cat")
            assertThat(populated.mode).isEqualTo(GenerateUsernameMode.CopyAndClose)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `regenerate produces a new username`() = runTest {
        viewModel.stateFlow.test {
            assertThat(awaitItem().username).isEqualTo("happy-cat")
            generator.result = "shiny-otter"
            viewModel.onRegenerate()
            assertThat(awaitItem().username).isEqualTo("shiny-otter")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `confirm writes plain string to draft and emits OnUsernameConfirmed`() = runTest {
        viewModel.stateFlow.test {
            awaitItem()
            viewModel.onConfirm()
            val state = awaitItem()
            assertThat(state.event).isEqualTo(GenerateUsernameEvent.OnUsernameConfirmed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `copy puts username on clipboard and emits OnUsernameCopied`() = runTest {
        viewModel.stateFlow.test {
            awaitItem()
            viewModel.onCopy()
            val state = awaitItem()
            assertThat(state.event).isEqualTo(GenerateUsernameEvent.OnUsernameCopied)
            assertThat(clipboard.getContents()).isEqualTo("happy-cat")
            cancelAndIgnoreRemainingEvents()
        }
    }
}
