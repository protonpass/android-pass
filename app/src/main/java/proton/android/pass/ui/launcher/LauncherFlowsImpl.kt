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

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountDeviceSecretNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.entity.AddAccountWorkflow
import me.proton.core.auth.presentation.onAddAccountResult
import me.proton.core.usersettings.presentation.UserSettingsOrchestrator
import proton.android.pass.data.api.usecases.DisableAccount
import proton.android.pass.data.api.usecases.GetPrimaryUserId
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.LogoutLogger
import proton.android.pass.log.api.LogoutReason
import proton.android.pass.telemetry.api.TelemetryGrowthSignupEvent
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

class LauncherFlowsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val authOrchestrator: AuthOrchestrator,
    private val userSettingsOrchestrator: UserSettingsOrchestrator,
    private val disableAccount: DisableAccount,
    private val getPrimaryUserId: GetPrimaryUserId,
    private val telemetryManager: TelemetryManager
) : LauncherFlows {

    override fun register(activity: ComponentActivity, scope: CoroutineScope) {
        authOrchestrator.register(activity as ActivityResultCaller)
        userSettingsOrchestrator.register(activity)

        authOrchestrator.onAddAccountResult { result ->
            if (result != null && result.workflow == AddAccountWorkflow.SignUp) {
                telemetryManager.sendEvent(TelemetryGrowthSignupEvent())
            }
            scope.launch {
                if (result == null && getPrimaryUserId() == null) {
                    activity.finish()
                }
            }
        }

        accountManager.observe(activity.lifecycle, Lifecycle.State.CREATED)
            .onAccountTwoPassModeFailed {
                LogoutLogger.record(LogoutReason.AccountSetupTwoPassFailed)
                disableAccount(it.userId)
            }
            .onAccountCreateAddressFailed {
                LogoutLogger.record(LogoutReason.AccountSetupCreateAddressFailed)
                disableAccount(it.userId)
            }
            .onSessionSecondFactorNeeded { authOrchestrator.startSecondFactorWorkflow(it) }
            .onAccountTwoPassModeNeeded { authOrchestrator.startTwoPassModeWorkflow(it) }
            .onAccountCreateAddressNeeded { authOrchestrator.startChooseAddressWorkflow(it) }
            .onAccountDeviceSecretNeeded { authOrchestrator.startDeviceSecretWorkflow(it) }
    }

    override fun startAddAccount() {
        authOrchestrator.startAddAccountWorkflow()
    }

    override fun startSignup() {
        authOrchestrator.startSignupWorkflow()
    }

    override fun startLogin(username: String?) {
        authOrchestrator.startLoginWorkflow(username)
    }

    override fun startPasswordManagement(userId: UserId) {
        userSettingsOrchestrator.startPasswordManagementWorkflow(userId)
    }

    override fun startRecoveryEmail(userId: UserId) {
        userSettingsOrchestrator.startUpdateRecoveryEmailWorkflow(userId)
    }

    override fun startSecurityKeys(userId: UserId) {
        userSettingsOrchestrator.startSecurityKeysWorkflow(userId)
    }
}
