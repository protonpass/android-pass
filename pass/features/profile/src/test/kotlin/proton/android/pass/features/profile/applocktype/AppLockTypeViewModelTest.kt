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

package proton.android.pass.features.profile.applocktype

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import android.content.Context
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.FakeBiometryManager
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.data.fakes.usecases.FakeClearPin
import proton.android.pass.data.fakes.usecases.FakeObserveAnyAccountHasEnforcedLock
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference.Biometrics
import proton.android.pass.preferences.AppLockTypePreference.None as NonePreference
import proton.android.pass.preferences.AppLockTypePreference.Pin
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class AppLockTypeViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var viewModel: AppLockTypeViewModel
    private lateinit var preferenceRepository: FakePreferenceRepository
    private lateinit var biometryManager: FakeBiometryManager
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var clearPin: FakeClearPin
    private lateinit var observeEnforcedLock: FakeObserveAnyAccountHasEnforcedLock
    private lateinit var appConfig: FakeAppConfig

    @Before
    fun setup() {
        preferenceRepository = FakePreferenceRepository()
        biometryManager = FakeBiometryManager()
        snackbarDispatcher = FakeSnackbarDispatcher()
        clearPin = FakeClearPin()
        observeEnforcedLock = FakeObserveAnyAccountHasEnforcedLock()
        appConfig = FakeAppConfig()

        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
    }

    private fun createViewModel() {
        viewModel = AppLockTypeViewModel(
            userPreferencesRepository = preferenceRepository,
            biometryManager = biometryManager,
            snackbarDispatcher = snackbarDispatcher,
            clearPin = clearPin,
            observeAnyAccountHasEnforcedLock = observeEnforcedLock,
            appConfig = appConfig
        )
    }

    @Test
    fun `regression case - biometrics enabled user selects None and authenticates - should fully disable`() = runTest {
        preferenceRepository.setAppLockTypePreference(Biometrics)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.selected).isEqualTo(Biometrics)
            assertThat(initialState.isPasswordOption).isFalse()

            val contextHolder = ClassHolder<Context>(None)
            viewModel.onChanged(NonePreference, contextHolder)

            biometryManager.emitResult(BiometryResult.Success)

            val finalState = awaitItem()
            assertThat(finalState.selected).isEqualTo(NonePreference)
            assertThat(finalState.event).isEqualTo(AppLockTypeEvent.Dismiss)

        }
    }

    @Test
    fun `pin enabled user selects None and authenticates with PIN - should fully disable`() = runTest {
        preferenceRepository.setAppLockTypePreference(Pin)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.selected).isEqualTo(Pin)
            assertThat(initialState.isPasswordOption).isFalse()

            val contextHolder = ClassHolder<Context>(None)
            viewModel.onChanged(NonePreference, contextHolder)

            val pinEntryState = awaitItem()
            assertThat(pinEntryState.event).isEqualTo(AppLockTypeEvent.EnterPin)

            viewModel.onPinSuccessfullyEntered(contextHolder)

            val finalState = awaitItem()
            assertThat(finalState.selected).isEqualTo(NonePreference)
            assertThat(finalState.event).isEqualTo(AppLockTypeEvent.Dismiss)

        }
    }

    @Test
    fun `already password-only mode - isPasswordOption reflects current state correctly`() = runTest {
        preferenceRepository.setAppLockTypePreference(NonePreference)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.selected).isEqualTo(NonePreference)
            assertThat(state.isPasswordOption).isTrue()
            assertThat(state.isForceLockMandatory).isFalse()
        }
    }

    @Test
    fun `biometrics not available - isPasswordOption false when none selected`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        preferenceRepository.setAppLockTypePreference(Pin)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.selected).isEqualTo(Pin)
            assertThat(state.isPasswordOption).isFalse()
        }
    }

    @Test
    fun `switching from pin to biometrics triggers biometry setup`() = runTest {
        preferenceRepository.setAppLockTypePreference(Pin)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.selected).isEqualTo(Pin)

            val contextHolder = ClassHolder<Context>(None)
            viewModel.onChanged(Biometrics, contextHolder)

            val pinPromptState = awaitItem()
            assertThat(pinPromptState.event).isEqualTo(AppLockTypeEvent.EnterPin)

            viewModel.onPinSuccessfullyEntered(contextHolder)

            biometryManager.emitResult(BiometryResult.Success)

            val finalState = awaitItem()
            assertThat(finalState.selected).isEqualTo(Biometrics)
            assertThat(finalState.event).isEqualTo(AppLockTypeEvent.Dismiss)
        }
    }

    @Test
    fun `selecting None when already None dismisses dialog`() = runTest {
        preferenceRepository.setAppLockTypePreference(NonePreference)
        preferenceRepository.setAppLockState(AppLockState.Disabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            awaitItem()

            val contextHolder = ClassHolder<Context>(None)
            viewModel.onChanged(NonePreference, contextHolder)

            val dismissState = awaitItem()
            assertThat(dismissState.event).isEqualTo(AppLockTypeEvent.Dismiss)
        }
    }

    @Test
    fun `biometrics to None without enforcement - full disable path`() = runTest {
        // This test exercises the onBiometryAuthUnSet path (full disable)
        // vs the onBiometryAuthToPassword path (password fallback)
        preferenceRepository.setAppLockTypePreference(Biometrics)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(OrganizationSettings.NotAnOrganization)

        createViewModel()

        viewModel.state.test {
            awaitItem()

            val contextHolder = ClassHolder<Context>(None)
            viewModel.onChanged(NonePreference, contextHolder)

            // When not enforced and not already in password mode,
            // biometry success -> onBiometryAuthUnSet (full disable)
            biometryManager.emitResult(BiometryResult.Success)

            val finalState = awaitItem()
            assertThat(finalState.selected).isEqualTo(NonePreference)
            assertThat(finalState.event).isEqualTo(AppLockTypeEvent.Dismiss)
        }
    }
}
