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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.preferences.DisplayFileAttachmentsBanner
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature

@RunWith(AndroidJUnit4::class)
class PeriodicFeatureDiscoveryWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var fakeObserveItemCount: FakeObserveItemCount
    private lateinit var fakePreferenceRepository: FakePreferenceRepository

    @Before
    fun setup() {
        fakeObserveItemCount = FakeObserveItemCount()
        fakePreferenceRepository = FakePreferenceRepository()
    }

    @Test
    fun returnsSuccessOnNormalExecution() = runTest {
        fakeObserveItemCount.sendResult(Result.success(ItemCountSummary.Initial))

        val result = buildWorker().doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun setsFileAttachmentsBannerToDisplayWhenItemCountExceedsThreshold() = runTest {
        val summary = summaryWithTotal(login = 11)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        assertThat(fakePreferenceRepository.observeDisplayFileAttachmentsOnboarding().first())
            .isEqualTo(DisplayFileAttachmentsBanner.Display)
    }

    @Test
    fun doesNotSetFileAttachmentsBannerWhenItemCountAtOrBelowThreshold() = runTest {
        val summary = summaryWithTotal(login = 10)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        assertThat(fakePreferenceRepository.observeDisplayFileAttachmentsOnboarding().first())
            .isEqualTo(DisplayFileAttachmentsBanner.Unknown)
    }

    @Test
    fun doesNotOverwriteFileAttachmentsBannerWhenAlreadySet() = runTest {
        fakePreferenceRepository.setDisplayFileAttachmentsOnboarding(DisplayFileAttachmentsBanner.NotDisplay)
        val summary = summaryWithTotal(login = 20)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        assertThat(fakePreferenceRepository.observeDisplayFileAttachmentsOnboarding().first())
            .isEqualTo(DisplayFileAttachmentsBanner.NotDisplay)
    }

    @Test
    fun setsAliasManagementBannersWhenAliasCountExceedsThreshold() = runTest {
        val summary = summaryWithAlias(alias = 3)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        FeatureDiscoveryFeature.entries.forEach { feature ->
            assertThat(
                fakePreferenceRepository.observeDisplayFeatureDiscoverBanner(feature).first()
            ).isEqualTo(FeatureDiscoveryBannerPreference.Display)
        }
    }

    @Test
    fun doesNotSetAliasManagementBannersWhenAliasCountAtOrBelowThreshold() = runTest {
        val summary = summaryWithAlias(alias = 2)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        FeatureDiscoveryFeature.entries.forEach { feature ->
            assertThat(
                fakePreferenceRepository.observeDisplayFeatureDiscoverBanner(feature).first()
            ).isEqualTo(FeatureDiscoveryBannerPreference.Unknown)
        }
    }

    @Test
    fun doesNotOverwriteAliasManagementBannersWhenAlreadySet() = runTest {
        val feature = FeatureDiscoveryFeature.AliasManagementContacts
        fakePreferenceRepository.setDisplayFeatureDiscoverBanner(
            feature,
            FeatureDiscoveryBannerPreference.NotDisplay
        )
        val summary = summaryWithAlias(alias = 10)
        fakeObserveItemCount.sendResult(Result.success(summary))

        buildWorker().doWork()

        assertThat(fakePreferenceRepository.observeDisplayFeatureDiscoverBanner(feature).first())
            .isEqualTo(FeatureDiscoveryBannerPreference.NotDisplay)
    }

    @Test
    fun doesNotSetAnyBannersWhenItemCountsAreBelowThresholds() = runTest {
        fakeObserveItemCount.sendResult(Result.success(ItemCountSummary.Initial))

        buildWorker().doWork()

        assertThat(fakePreferenceRepository.observeDisplayFileAttachmentsOnboarding().first())
            .isEqualTo(DisplayFileAttachmentsBanner.Unknown)
        FeatureDiscoveryFeature.entries.forEach { feature ->
            assertThat(
                fakePreferenceRepository.observeDisplayFeatureDiscoverBanner(feature).first()
            ).isEqualTo(FeatureDiscoveryBannerPreference.Unknown)
        }
    }

    private fun buildWorker(): PeriodicFeatureDiscoveryWorker =
        TestListenableWorkerBuilder<PeriodicFeatureDiscoveryWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = PeriodicFeatureDiscoveryWorker(
                    appContext = appContext,
                    workerParameters = workerParameters,
                    observeItemCount = fakeObserveItemCount,
                    userPreferencesRepository = fakePreferenceRepository
                )
            })
            .build()

    companion object {
        private fun summaryWithTotal(login: Long): ItemCountSummary =
            ItemCountSummary.Initial.copy(login = login)

        private fun summaryWithAlias(alias: Long): ItemCountSummary =
            ItemCountSummary.Initial.copy(alias = alias)
    }
}
