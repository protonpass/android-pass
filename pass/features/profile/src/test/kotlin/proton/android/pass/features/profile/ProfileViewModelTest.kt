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

package proton.android.pass.features.profile

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.autofill.fakes.FakeAutofillManager
import proton.android.pass.biometry.FakeBiometryManager
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.SetAppLockType
import proton.android.pass.data.fakes.usecases.FakeCanCreateAlias
import proton.android.pass.data.fakes.usecases.FakeObserveAccounts
import proton.android.pass.data.fakes.usecases.FakeObserveAnyAccountHasEnforcedLock
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObserveMFACount
import proton.android.pass.data.fakes.usecases.FakeObservePrimaryAccount
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveUser
import proton.android.pass.data.fakes.usecases.FakeObserveUserAccessData
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.data.fakes.usecases.FakeSetPasswordOnlyLock
import proton.android.pass.data.fakes.usecases.securelink.FakeObserveSecureLinksCount
import proton.android.pass.domain.ForceLockSeconds
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.domain.OrganizationShareMode
import proton.android.pass.domain.organizations.OrganizationAliasMode
import proton.android.pass.domain.organizations.OrganizationItemShareMode
import proton.android.pass.domain.organizations.OrganizationPasswordPolicy
import proton.android.pass.domain.organizations.OrganizationSecureLinkMode
import proton.android.pass.domain.organizations.OrganizationSharingPolicy
import proton.android.pass.domain.organizations.OrganizationVaultCreateMode
import proton.android.pass.domain.organizations.OrganizationVaultsPolicy
import proton.android.pass.features.profile.applocktype.PendingAppLockChangeHolder
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.passkeys.fakes.FakeCanDeviceSupportPasskeys
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.BiometricSystemLockPreference
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.searchoptions.fakes.FakeHomeSearchOptionsRepository
import proton.android.pass.test.MainDispatcherRule

class ProfileViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var preferenceRepository: FakePreferenceRepository
    private lateinit var biometryManager: FakeBiometryManager
    private lateinit var pendingAppLockChangeHolder: PendingAppLockChangeHolder
    private lateinit var setAppLockType: RecordingSetAppLockType
    private lateinit var observeEnforcedLock: FakeObserveAnyAccountHasEnforcedLock
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        preferenceRepository = FakePreferenceRepository()
        biometryManager = FakeBiometryManager()
        pendingAppLockChangeHolder = PendingAppLockChangeHolder()
        setAppLockType = RecordingSetAppLockType(preferenceRepository)
        observeEnforcedLock = FakeObserveAnyAccountHasEnforcedLock()
        val observeItemCount = FakeObserveItemCount().apply {
            sendResult(Result.success(EMPTY_ITEM_COUNT))
        }
        val observeMFACount = FakeObserveMFACount().apply { emitResult(0) }
        val observeUserAccessData = FakeObserveUserAccessData().apply { sendValue(null) }
        viewModel = ProfileViewModel(
            userPreferencesRepository = preferenceRepository,
            autofillManager = FakeAutofillManager(),
            biometryManager = biometryManager,
            pendingAppLockChangeHolder = pendingAppLockChangeHolder,
            setAppLockType = setAppLockType,
            setPasswordOnlyLock = FakeSetPasswordOnlyLock(),
            clipboardManager = FakeClipboardManager(),
            snackbarDispatcher = FakeSnackbarDispatcher(),
            appConfig = FakeAppConfig(),
            canDeviceSupportPasskeys = FakeCanDeviceSupportPasskeys(),
            observeUser = FakeObserveUser(),
            performSync = FakePerformSync(),
            searchOptionsRepository = FakeHomeSearchOptionsRepository(),
            observeAccounts = FakeObserveAccounts(),
            observePrimaryAccount = FakeObservePrimaryAccount(),
            observeItemCount = observeItemCount,
            observeMFACount = observeMFACount,
            observeUpgradeInfo = FakeObserveUpgradeInfo(),
            observeAnyAccountHasEnforcedLock = observeEnforcedLock,
            observeSecureLinksCount = FakeObserveSecureLinksCount(),
            observeUserAccessData = observeUserAccessData,
            canCreateAlias = FakeCanCreateAlias()
        )
    }

    @Test
    fun `toggle is locked and cannot be turned off when biometric lock has no strong biometric`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.Enabled, biometricsOnly = false)

        assertThat(awaitBiometricSection().isSystemLockToggleLocked).isTrue()

        viewModel.onToggleBiometricSystemLock(false)
        advanceUntilIdle()

        assertThat(systemLockPreference()).isEqualTo(BiometricSystemLockPreference.Enabled)
    }

    @Test
    fun `toggle is unlocked and can be turned off when a strong biometric is available`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.Enabled, biometricsOnly = true)

        assertThat(awaitBiometricSection().isSystemLockToggleLocked).isFalse()

        viewModel.onToggleBiometricSystemLock(false)
        advanceUntilIdle()

        assertThat(systemLockPreference()).isEqualTo(BiometricSystemLockPreference.NotEnabled)
    }

    @Test
    fun `toggle can be turned on even when no strong biometric is available`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.NotEnabled, biometricsOnly = false)

        assertThat(awaitBiometricSection().isSystemLockToggleLocked).isFalse()

        viewModel.onToggleBiometricSystemLock(true)
        advanceUntilIdle()

        assertThat(systemLockPreference()).isEqualTo(BiometricSystemLockPreference.Enabled)
    }

    @Test
    fun `resume recomputes the lock after biometrics are enrolled`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.Enabled, biometricsOnly = false)
        assertThat(awaitBiometricSection { it.isSystemLockToggleLocked }.isSystemLockToggleLocked).isTrue()

        biometryManager.biometricsOnlyAuthenticationAvailable = true
        viewModel.onScreenResumed()

        assertThat(awaitBiometricSection { !it.isSystemLockToggleLocked }.isSystemLockToggleLocked).isFalse()
    }

    @Test
    fun `resume recomputes the lock after the last biometric is removed`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.Enabled, biometricsOnly = true)
        assertThat(awaitBiometricSection { !it.isSystemLockToggleLocked }.isSystemLockToggleLocked).isFalse()

        biometryManager.biometricsOnlyAuthenticationAvailable = false
        viewModel.onScreenResumed()

        assertThat(awaitBiometricSection { it.isSystemLockToggleLocked }.isSystemLockToggleLocked).isTrue()
    }

    @Test
    fun `verified master password without a pending target does not change the lock`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.NotEnabled, biometricsOnly = false)
        pendingAppLockChangeHolder.pendingTarget = null

        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()

        assertThat(setAppLockType.invoked).isEmpty()
        assertThat(preferenceRepository.getAppLockTypePreference().first())
            .isEqualTo(AppLockTypePreference.Biometrics)
    }

    @Test
    fun `verified master password with a pending target applies it once`() = runTest {
        givenBiometricLock(systemLock = BiometricSystemLockPreference.NotEnabled, biometricsOnly = false)
        pendingAppLockChangeHolder.pendingTarget = AppLockTypePreference.None

        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()
        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()

        assertThat(setAppLockType.invoked).containsExactly(AppLockTypePreference.None)
        assertThat(pendingAppLockChangeHolder.pendingTarget).isNull()
    }

    @Test
    fun `verified master password disables a password only lock without enforcement`() = runTest {
        preferenceRepository.setAppLockTypePreference(AppLockTypePreference.None)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        pendingAppLockChangeHolder.pendingTarget = AppLockTypePreference.None

        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()

        assertThat(setAppLockType.invoked).containsExactly(AppLockTypePreference.None)
        assertThat(preferenceRepository.getAppLockState().first()).isEqualTo(AppLockState.Disabled)
    }

    @Test
    fun `verified master password does not disable a password only lock under enforcement`() = runTest {
        preferenceRepository.setAppLockTypePreference(AppLockTypePreference.None)
        preferenceRepository.setAppLockState(AppLockState.Enabled)
        observeEnforcedLock.emitValue(
            OrganizationSettings.Organization(
                canUpdate = true,
                shareMode = OrganizationShareMode.Unrestricted,
                forceLockSeconds = ForceLockSeconds.Enforced(600),
                passwordPolicy = OrganizationPasswordPolicy(),
                vaultsPolicy = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.AllUsers
                ),
                sharingPolicy = OrganizationSharingPolicy(
                    itemShareMode = OrganizationItemShareMode.Enabled,
                    secureLinkMode = OrganizationSecureLinkMode.Enabled
                ),
                aliasMode = OrganizationAliasMode.AllowedForAllMembers
            )
        )
        pendingAppLockChangeHolder.pendingTarget = AppLockTypePreference.None

        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()

        assertThat(setAppLockType.invoked).isEmpty()
        assertThat(preferenceRepository.getAppLockState().first()).isEqualTo(AppLockState.Enabled)
    }

    @Test
    fun `verified master password does nothing when the lock is not password only`() = runTest {
        preferenceRepository.setAppLockTypePreference(AppLockTypePreference.None)
        preferenceRepository.setAppLockState(AppLockState.Disabled)
        pendingAppLockChangeHolder.pendingTarget = AppLockTypePreference.None

        viewModel.onMasterPasswordVerifiedForLockChange()
        advanceUntilIdle()

        assertThat(setAppLockType.invoked).isEmpty()
    }

    private fun givenBiometricLock(systemLock: BiometricSystemLockPreference, biometricsOnly: Boolean) {
        preferenceRepository.setAppLockTypePreference(AppLockTypePreference.Biometrics)
        preferenceRepository.setBiometricSystemLockPreference(systemLock)
        biometryManager.biometricsOnlyAuthenticationAvailable = biometricsOnly
    }

    private suspend fun awaitBiometricSection(predicate: (BiometricSection) -> Boolean = { true }): BiometricSection =
        viewModel.state
            .first { (it.appLockSectionState as? BiometricSection)?.let(predicate) == true }
            .appLockSectionState as BiometricSection

    private suspend fun systemLockPreference(): BiometricSystemLockPreference =
        preferenceRepository.getBiometricSystemLockPreference().first()

    private companion object {
        val EMPTY_ITEM_COUNT = ItemCountSummary(
            login = 0,
            loginWithMFA = 0,
            note = 0,
            alias = 0,
            creditCard = 0,
            identities = 0,
            custom = 0,
            sharedWithMe = 0,
            sharedByMe = 0,
            trashed = 0,
            sharedWithMeTrashed = 0
        )
    }
}

private class RecordingSetAppLockType(
    private val userPreferencesRepository: UserPreferencesRepository
) : SetAppLockType {

    val invoked = mutableListOf<AppLockTypePreference>()

    override suspend fun invoke(type: AppLockTypePreference): Result<Unit> = runCatching {
        invoked += type
        userPreferencesRepository.setAppLockTypePreference(type).getOrThrow()
        val state = if (type == AppLockTypePreference.None) AppLockState.Disabled else AppLockState.Enabled
        userPreferencesRepository.setAppLockState(state).getOrThrow()
    }
}
