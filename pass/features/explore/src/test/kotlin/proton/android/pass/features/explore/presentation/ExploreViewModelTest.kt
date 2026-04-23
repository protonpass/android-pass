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

package proton.android.pass.features.explore.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveLoginTotpEntries
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.features.explore.navigation.ExploreNavDestination
import proton.android.pass.features.explore.telemetry.PassExploreBusinessBannerClick
import proton.android.pass.features.explore.telemetry.PassExploreDisplayHome
import proton.android.pass.features.explore.telemetry.PassExploreProductClick
import proton.android.pass.features.explore.telemetry.PassExploreToolClick
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.ProtonAppType
import proton.android.pass.protonapps.api.usecases.OpenResult
import proton.android.pass.protonapps.fakes.FakeObserveProtonApps
import proton.android.pass.protonapps.fakes.FakeOpenProtonApp
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

internal class ExploreViewModelTest {

    @get:Rule
    internal val dispatcher = MainDispatcherRule()

    private lateinit var observeProtonApps: FakeObserveProtonApps
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var observeLoginTotpEntries: FakeObserveLoginTotpEntries
    private lateinit var openProtonApp: FakeOpenProtonApp
    private lateinit var telemetryManager: FakeTelemetryManager

    private lateinit var instance: ExploreViewModel

    @Before
    fun setup() {
        observeProtonApps = FakeObserveProtonApps()
        getUserPlan = FakeGetUserPlan().apply {
            setResult(Result.success(freePlan()))
        }
        observeLoginTotpEntries = FakeObserveLoginTotpEntries().apply { emit(emptyList()) }
        openProtonApp = FakeOpenProtonApp()
        telemetryManager = FakeTelemetryManager()
        createViewModel()
    }

    private fun createViewModel() {
        instance = ExploreViewModel(
            observeProtonApps = observeProtonApps,
            getUserPlan = getUserPlan,
            observeLoginTotpEntries = observeLoginTotpEntries,
            openProtonApp = openProtonApp,
            telemetryManager = telemetryManager
        )
    }

    @Test
    fun `emits PassExploreDisplayHome once at init`() {
        val displayEvents = telemetryManager.getMemory().filter { it == PassExploreDisplayHome }
        assertThat(displayEvents).hasSize(1)
    }

    @Test
    fun `PasswordGeneratorClicked emits telemetry and nav event`() = runTest {
        instance.navEvents.test {
            instance.onEvent(ExploreUiEvent.PasswordGeneratorClicked)
            assertThat(awaitItem()).isEqualTo(ExploreNavDestination.PasswordGenerator)
        }
        assertThat(telemetryManager.getMemory())
            .contains(PassExploreToolClick(PassExploreToolClick.Tool.PasswordGenerator))
    }

    @Test
    fun `ProductClicked with installed app emits open_app action`() = runTest {
        observeProtonApps.emit(listOf(ProtonApp(type = ProtonAppType.Vpn, isInstalled = true)))
        openProtonApp.setResult(OpenResult.OpenedApp)

        instance.uiState.test {
            val state = awaitStateWithApps()
            assertThat(state.protonApps).isNotEmpty()

            instance.onEvent(ExploreUiEvent.ProductClicked(ProtonAppType.Vpn))
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(telemetryManager.getMemory())
            .contains(
                PassExploreProductClick(
                    product = ProtonAppType.Vpn,
                    action = PassExploreProductClick.Action.OpenApp
                )
            )
    }

    @Test
    fun `ProductClicked with non-installed app emits open_store action`() = runTest {
        observeProtonApps.emit(listOf(ProtonApp(type = ProtonAppType.Vpn, isInstalled = false)))
        openProtonApp.setResult(OpenResult.OpenedPlayStore)

        instance.uiState.test {
            val state = awaitStateWithApps()
            assertThat(state.protonApps).isNotEmpty()

            instance.onEvent(ExploreUiEvent.ProductClicked(ProtonAppType.Vpn))
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(telemetryManager.getMemory())
            .contains(
                PassExploreProductClick(
                    product = ProtonAppType.Vpn,
                    action = PassExploreProductClick.Action.OpenStore
                )
            )
    }

    @Test
    fun `BusinessBannerClicked emits telemetry and external URL`() = runTest {
        instance.navEvents.test {
            instance.onEvent(ExploreUiEvent.BusinessBannerClicked)
            assertThat(awaitItem()).isEqualTo(
                ExploreNavDestination.ExternalUrl(ExploreViewModel.PROTON_FOR_BUSINESS_URL)
            )
        }
        assertThat(telemetryManager.getMemory()).contains(PassExploreBusinessBannerClick)
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<ExploreUiState>.awaitStateWithApps(): ExploreUiState {
        var state = awaitItem()
        while (state.protonApps.isEmpty()) {
            state = awaitItem()
        }
        return state
    }

    private fun freePlan(): Plan = Plan(
        planType = PlanType.Free("free", "Proton Free"),
        hideUpgrade = false,
        vaultLimit = PlanLimit.Limited(10),
        aliasLimit = PlanLimit.Limited(10),
        totpLimit = PlanLimit.Limited(10),
        updatedAt = 0L
    )
}
