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

package proton.android.pass.features.password.history

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeleteOnePasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeletePasswordHistoryEntryForUser
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeObservePasswordHistoryEntryForUser
import proton.android.pass.domain.PasswordHistoryEntry
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.password.history.model.PasswordDateLabel
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.FixedClock
import proton.android.pass.test.MainDispatcherRule
import kotlin.time.Duration.Companion.days

internal class PasswordHistoryEntryViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private val fixedClock = FixedClock(Instant.parse("2026-01-15T12:00:00Z"))

    private lateinit var observePasswordHistory: FakeObservePasswordHistoryEntryForUser
    private lateinit var deleteAllHistory: FakeDeletePasswordHistoryEntryForUser
    private lateinit var deleteOneHistory: FakeDeleteOnePasswordHistoryEntryForUser
    private lateinit var clipboardManager: FakeClipboardManager
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var viewModel: PasswordHistoryEntryViewModel

    @Before
    internal fun setUp() {
        observePasswordHistory = FakeObservePasswordHistoryEntryForUser()
        deleteAllHistory = FakeDeletePasswordHistoryEntryForUser()
        deleteOneHistory = FakeDeleteOnePasswordHistoryEntryForUser()
        clipboardManager = FakeClipboardManager()
        snackbarDispatcher = FakeSnackbarDispatcher()

        viewModel = PasswordHistoryEntryViewModel(
            observePasswordHistoryForUser = observePasswordHistory,
            clock = fixedClock,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            deletePasswordHistoryEntryForUser = deleteAllHistory,
            deleteOnePasswordHistoryEntryForUser = deleteOneHistory,
            clipboardManager = clipboardManager,
            snackbarDispatcher = snackbarDispatcher
        )
    }

    @Test
    internal fun `WHEN view model is initialized THEN initial state is loading`() {
        assertThat(viewModel.state.value.isLoading).isTrue()
    }

    @Test
    internal fun `WHEN observe emits empty list THEN state shows not loading with no items`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.items).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN entry created today THEN date label is Today`() = runTest {
        observePasswordHistory.sendResult(Result.success(listOf(entryAt(fixedClock.instant))))

        viewModel.state.test {
            assertThat(awaitItem().items.first().dateLabel).isInstanceOf(PasswordDateLabel.Today::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN entry created yesterday THEN date label is Yesterday`() = runTest {
        observePasswordHistory.sendResult(Result.success(listOf(entryAt(fixedClock.instant - 1.days))))

        viewModel.state.test {
            assertThat(awaitItem().items.first().dateLabel).isInstanceOf(PasswordDateLabel.Yesterday::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN entry created 5 days ago THEN date label is DaysAgo with correct count`() = runTest {
        observePasswordHistory.sendResult(Result.success(listOf(entryAt(fixedClock.instant - 5.days))))

        viewModel.state.test {
            val label = awaitItem().items.first().dateLabel
            assertThat(label).isInstanceOf(PasswordDateLabel.DaysAgo::class.java)
            assertThat((label as PasswordDateLabel.DaysAgo).days).isEqualTo(5)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN items list is empty THEN options menu is not visible`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.state.test {
            assertThat(awaitItem().isOptionsMenuVisible).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN items list is not empty THEN options menu is visible`() = runTest {
        observePasswordHistory.sendResult(Result.success(listOf(entryAt(fixedClock.instant))))

        viewModel.state.test {
            assertThat(awaitItem().isOptionsMenuVisible).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN onHideItem called with unknown id THEN state items remain empty`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.onHideItem(PasswordHistoryEntryId(999))

        viewModel.state.test {
            assertThat(awaitItem().items).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN onRevealItem called with unknown id THEN state items remain empty`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.onRevealItem(PasswordHistoryEntryId(999))

        viewModel.state.test {
            assertThat(awaitItem().items).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    internal fun `WHEN onCopyPassword called with unknown id THEN clipboard is not updated`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.onCopyPassword(PasswordHistoryEntryId(999))

        assertThat(clipboardManager.getContents()).isEmpty()
    }

    @Test
    internal fun `WHEN onCopyPassword called with unknown id THEN no snackbar is shown`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.onCopyPassword(PasswordHistoryEntryId(999))

        assertThat(snackbarDispatcher.invocationCount).isEqualTo(0)
    }

    @Test
    internal fun `WHEN onClearHistory is called THEN delete all use case is invoked`() = runTest {
        viewModel.onClearHistory()

        assertThat(deleteAllHistory.invocationCount).isEqualTo(1)
    }

    @Test
    internal fun `WHEN onClearHistory called multiple times THEN delete all use case is invoked each time`() = runTest {
        viewModel.onClearHistory()
        viewModel.onClearHistory()

        assertThat(deleteAllHistory.invocationCount).isEqualTo(2)
    }

    @Test
    internal fun `WHEN onClearItem called with unknown id THEN delete one use case is not invoked`() = runTest {
        observePasswordHistory.sendResult(Result.success(emptyList()))

        viewModel.onClearItem(PasswordHistoryEntryId(999))

        assertThat(deleteOneHistory.deletedIds).isEmpty()
    }

    private fun entryAt(instant: Instant) = PasswordHistoryEntry(
        passwordHistoryEntryId = PasswordHistoryEntryId(0),
        encrypted = "any",
        createdTime = instant.epochSeconds
    )
}
