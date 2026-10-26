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

package proton.android.pass.ui.launcher

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.biometry.FakeResetAuthPreferences
import proton.android.pass.commonrust.api.CommonLibraryVersionChecker
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.fakes.usecases.FakeDisableAccount
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.fakes.usecases.FakeGetReadyAccounts
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeInitialWorkerLauncher
import proton.android.pass.data.fakes.usecases.FakeObserveAccounts
import proton.android.pass.data.fakes.usecases.FakeRefreshUserAccess
import proton.android.pass.data.fakes.usecases.FakeSetAsPrimary
import proton.android.pass.data.fakes.usecases.passwordHistoryEntry.FakeDeletePasswordHistoryEntryForUser
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.AccountState as PassAccountState
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.AccountTestFactory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class LauncherViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val dispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var observeAccounts: FakeObserveAccounts
    private lateinit var getReadyAccounts: FakeGetReadyAccounts
    private lateinit var disableAccount: FakeDisableAccount
    private lateinit var setAsPrimary: FakeSetAsPrimary
    private lateinit var launcherFlows: FakeLauncherFlows
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var refreshUserAccess: FakeRefreshUserAccess
    private lateinit var inAppUpdatesManager: FakeInAppUpdatesManager
    private lateinit var deletePasswordHistory: FakeDeletePasswordHistoryEntryForUser
    private lateinit var settingsRepository: FakeInternalSettingsRepository
    private lateinit var preferences: FakePreferenceRepository
    private lateinit var appConfig: FakeAppConfig
    private lateinit var getPrimaryUserId: FakeGetPrimaryUserId
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var instance: LauncherViewModel

    @Before
    fun setup() {
        observeAccounts = FakeObserveAccounts()
        getReadyAccounts = FakeGetReadyAccounts()
        disableAccount = FakeDisableAccount()
        setAsPrimary = FakeSetAsPrimary()
        launcherFlows = FakeLauncherFlows()
        getUserPlan = FakeGetUserPlan()
        refreshUserAccess = FakeRefreshUserAccess()
        inAppUpdatesManager = FakeInAppUpdatesManager()
        deletePasswordHistory = FakeDeletePasswordHistoryEntryForUser()
        settingsRepository = FakeInternalSettingsRepository()
        preferences = FakePreferenceRepository()
        appConfig = FakeAppConfig()
        getPrimaryUserId = FakeGetPrimaryUserId()
        snackbarDispatcher = FakeSnackbarDispatcher()

        instance = newInstance()
    }

    private fun newInstance() = LauncherViewModel(
        accountManager = FakeAccountManager(),
        observeAccounts = observeAccounts,
        getReadyAccounts = getReadyAccounts,
        disableAccount = disableAccount,
        setAsPrimaryAccount = setAsPrimary,
        launcherFlows = launcherFlows,
        initialWorkerLauncher = FakeInitialWorkerLauncher(),
        getUserPlan = getUserPlan,
        refreshUserAccess = refreshUserAccess,
        inAppUpdatesManager = inAppUpdatesManager,
        resetUserPreferences = FakeResetAuthPreferences(),
        snackbarDispatcher = snackbarDispatcher,
        deletePasswordHistoryEntryForUser = deletePasswordHistory,
        settingsRepository = settingsRepository,
        userPreferencesRepository = preferences,
        commonLibraryVersionChecker = object : CommonLibraryVersionChecker {
            override fun getVersion() = "1.0"
        },
        appConfig = appConfig,
        appDispatchers = FakeAppDispatchers.withTestDispatcher(testDispatcher),
        getPrimaryUserId = getPrimaryUserId
    )

    @Test
    fun `state is PrimaryExist when a ready account exists`() = runTest {
        val state = instance.state.first { it.accountState != AccountState.Processing }

        assertThat(state.accountState).isEqualTo(AccountState.PrimaryExist)
    }

    @Test
    fun `state is AccountNeeded when there are no accounts`() = runTest {
        observeAccounts.emitAccounts(emptyList())

        val state = instance.state.first { it.accountState != AccountState.Processing }

        assertThat(state.accountState).isEqualTo(AccountState.AccountNeeded)
    }

    @Test
    fun `state carries the theme preference`() = runTest {
        preferences.setThemePreference(ThemePreference.Light)

        val state = instance.state.first { it.accountState != AccountState.Processing }

        assertThat(state.themePreference).isEqualTo(ThemePreference.Light)
    }

    @Test
    fun `state supports payment on play and not on fdroid`() = runTest {
        val play = instance.state.first { it.accountState != AccountState.Processing }
        assertThat(play.supportPayment).isTrue()

        appConfig.setFlavor(BuildFlavor.Fdroid(BuildEnv.PROD))
        val fdroid = newInstance().state.first { it.accountState != AccountState.Processing }
        assertThat(fdroid.supportPayment).isFalse()
    }

    @Test
    fun `reload warning is shown until it has been dismissed`() = runTest {
        val before = instance.state.first { it.accountState != AccountState.Processing }
        assertThat(before.canShowWarningReloadApp).isTrue()

        instance.doNotDisplayReloadAppWarningDialog()

        val after = instance.state.first { !it.canShowWarningReloadApp }
        assertThat(after.canShowWarningReloadApp).isFalse()
    }

    @Test
    fun `switch to a disabled account starts login with its username`() = runTest {
        observeAccounts.emitAccounts(
            listOf(AccountTestFactory.create(OTHER_USER, PassAccountState.Disabled).copy(username = "alice"))
        )

        instance.switch(OTHER_USER)

        assertThat(launcherFlows.calls).containsExactly("login:alice")
        assertThat(setAsPrimary.primaryUserIds).isEmpty()
        assertThat(deletePasswordHistory.invocationCount).isEqualTo(1)
    }

    @Test
    fun `switch to a ready account sets it as primary`() = runTest {
        observeAccounts.emitAccounts(listOf(AccountTestFactory.create(OTHER_USER, PassAccountState.Ready)))

        instance.switch(OTHER_USER)

        assertThat(setAsPrimary.primaryUserIds).containsExactly(OTHER_USER)
        assertThat(launcherFlows.calls).isEmpty()
        assertThat(deletePasswordHistory.invocationCount).isEqualTo(1)
    }

    @Test
    fun `switch to an unknown account does nothing`() = runTest {
        instance.switch(OTHER_USER)

        assertThat(setAsPrimary.primaryUserIds).isEmpty()
        assertThat(launcherFlows.calls).isEmpty()
        assertThat(deletePasswordHistory.invocationCount).isEqualTo(0)
    }

    @Test
    fun `disable disables only the given account`() = runTest {
        instance.disable(OTHER_USER)

        assertThat(disableAccount.disabledUserIds).containsExactly(OTHER_USER)
    }

    @Test
    fun `disableAll disables every ready account`() = runTest {
        val second = UserId("second-user")
        getReadyAccounts.setResult(
            listOf(AccountTestFactory.create(OTHER_USER), AccountTestFactory.create(second))
        )

        instance.disableAll()

        assertThat(disableAccount.disabledUserIds).containsExactly(OTHER_USER, second).inOrder()
    }

    @Test
    fun `onAccountNeeded starts the add account flow`() = runTest {
        instance.onAccountNeeded()

        assertThat(launcherFlows.calls).containsExactly("addAccount")
    }

    @Test
    fun `settings flows start for the primary user`() = runTest {
        val primary = UserId(FakeGetPrimaryUserId.DEFAULT_USER_ID)

        instance.passwordManagement()
        instance.recoveryEmail()
        instance.securityKeys()

        assertThat(launcherFlows.calls).containsExactly(
            "passwordManagement:${primary.id}",
            "recoveryEmail:${primary.id}",
            "securityKeys:${primary.id}"
        ).inOrder()
    }

    @Test
    fun `settings flows do nothing without a primary user`() = runTest {
        getPrimaryUserId.setResult(null)

        instance.passwordManagement()
        instance.recoveryEmail()
        instance.securityKeys()

        assertThat(launcherFlows.calls).isEmpty()
    }

    @Test
    fun `tryUpgrade stops refreshing once the plan changes`() = runTest {
        val userId = UserId("upgrade-user")
        getUserPlan.setResult(Result.success(plan("free")), userId)
        refreshUserAccess.onInvoke = { getUserPlan.setResult(Result.success(plan("plus")), userId) }

        instance.tryUpgrade(userId)
        testDispatcher.scheduler.advanceTimeBy(20.seconds)

        assertThat(refreshUserAccess.invocations).isEqualTo(1)
    }

    @Test
    fun `tryUpgrade gives up after three attempts when the plan never changes`() = runTest {
        val userId = UserId("upgrade-user")
        getUserPlan.setResult(Result.success(plan("free")), userId)

        instance.tryUpgrade(userId)
        testDispatcher.scheduler.advanceTimeBy(20.seconds)

        assertThat(refreshUserAccess.invocations).isEqualTo(3)
    }

    @Test
    fun `tryUpgrade waits an increasing delay between attempts`() = runTest {
        val userId = UserId("upgrade-user")
        getUserPlan.setResult(Result.success(plan("free")), userId)

        instance.tryUpgrade(userId)
        testDispatcher.scheduler.advanceTimeBy(1.seconds)
        assertThat(refreshUserAccess.invocations).isEqualTo(0)

        testDispatcher.scheduler.advanceTimeBy(1.seconds + 1.milliseconds)
        assertThat(refreshUserAccess.invocations).isEqualTo(1)

        testDispatcher.scheduler.advanceTimeBy(4.seconds)
        assertThat(refreshUserAccess.invocations).isEqualTo(2)
    }

    @Test
    fun `onPrimaryExist checks for updates`() = runTest {
        instance.onPrimaryExist(NoOpLauncher)

        assertThat(inAppUpdatesManager.calls).containsExactly("checkForUpdates")
    }

    @Test
    fun `update listener calls are forwarded`() = runTest {
        instance.cancelUpdateListener()
        instance.declineUpdate()

        assertThat(inAppUpdatesManager.calls).containsExactly("tearDown", "declineUpdate").inOrder()
    }

    private fun plan(name: String) = FakeGetUserPlan.DEFAULT_PLAN.copy(planType = PlanType.Free(name, name))

    private companion object {
        val OTHER_USER = UserId("other-user")

        val NoOpLauncher = object : ActivityResultLauncher<IntentSenderRequest>() {
            override val contract: ActivityResultContract<IntentSenderRequest, *>
                get() = throw UnsupportedOperationException()

            override fun launch(input: IntentSenderRequest, options: ActivityOptionsCompat?) = Unit

            override fun unregister() = Unit
        }
    }
}
