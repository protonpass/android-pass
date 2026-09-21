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

package proton.android.pass.features.searchoptions

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObservePinnedItemCount
import proton.android.pass.data.fakes.usecases.items.FakeObserveSharedItemCountSummary
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.searchoptions.fakes.FakeHomeSearchOptionsRepository
import proton.android.pass.test.MainDispatcherRule

internal class FilterBottomSheetViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeItemCount: FakeObserveItemCount
    private lateinit var observePinnedItemCount: FakeObservePinnedItemCount
    private lateinit var observeSharedItemCountSummary: FakeObserveSharedItemCountSummary
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository
    private lateinit var homeSearchOptionsRepository: FakeHomeSearchOptionsRepository

    @Before
    internal fun setup() {
        observeItemCount = FakeObserveItemCount()
        observePinnedItemCount = FakeObservePinnedItemCount()
        observeSharedItemCountSummary = FakeObserveSharedItemCountSummary()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
        homeSearchOptionsRepository = FakeHomeSearchOptionsRepository()

        observeItemCount.sendResult(Result.success(ItemCountSummary.Initial))
        observePinnedItemCount.sendResult(Result.success(ItemCountSummary.Initial))
        observeSharedItemCountSummary.emit(ItemCountSummary.Initial)
    }

    @Test
    internal fun `when vault selected with PASS_FOLDERS enabled, restrictToRootFolder is true`() = runTest {
        val shareId = ShareId("share-1")
        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)

        val viewModel = FilterBottomSheetViewModel(
            observeItemCount = observeItemCount,
            observePinnedItemCount = observePinnedItemCount,
            observeSharedItemCountSummary = observeSharedItemCountSummary,
            featureFlagsPreferencesRepository = featureFlags,
            homeSearchOptionsRepository = homeSearchOptionsRepository
        )

        homeSearchOptionsRepository.setVaultSelectionOption(
            VaultSelectionOption.Vault(shareId = shareId)
        )

        viewModel.stateFlow.test {
            awaitItem()

            assertThat(observeItemCount.lastRestrictToRootFolder).isTrue()
        }
    }

    @Test
    internal fun `when vault selected with PASS_FOLDERS disabled, restrictToRootFolder is false`() = runTest {
        val shareId = ShareId("share-1")
        featureFlags.set(FeatureFlag.PASS_FOLDERS, false)

        val viewModel = FilterBottomSheetViewModel(
            observeItemCount = observeItemCount,
            observePinnedItemCount = observePinnedItemCount,
            observeSharedItemCountSummary = observeSharedItemCountSummary,
            featureFlagsPreferencesRepository = featureFlags,
            homeSearchOptionsRepository = homeSearchOptionsRepository
        )

        homeSearchOptionsRepository.setVaultSelectionOption(
            VaultSelectionOption.Vault(shareId = shareId)
        )

        viewModel.stateFlow.test {
            awaitItem()

            assertThat(observeItemCount.lastRestrictToRootFolder).isFalse()
        }
    }

    @Test
    internal fun `when folder selected, restrictToRootFolder flag does not affect behavior`() = runTest {
        val shareId = ShareId("share-1")
        val folderId = FolderId("folder-1")
        featureFlags.set(FeatureFlag.PASS_FOLDERS, true)

        val viewModel = FilterBottomSheetViewModel(
            observeItemCount = observeItemCount,
            observePinnedItemCount = observePinnedItemCount,
            observeSharedItemCountSummary = observeSharedItemCountSummary,
            featureFlagsPreferencesRepository = featureFlags,
            homeSearchOptionsRepository = homeSearchOptionsRepository
        )

        homeSearchOptionsRepository.setVaultSelectionOption(
            VaultSelectionOption.Folder(shareId = shareId, folderId = folderId)
        )

        viewModel.stateFlow.test {
            awaitItem()

            assertThat(observeItemCount.lastShareSelection).isNotNull()
        }
    }

}
