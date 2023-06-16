/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.account.impl

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.plan.presentation.PlansOrchestrator
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import javax.inject.Inject

class AccountOrchestratorsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val plansOrchestrator: PlansOrchestrator
) : AccountOrchestrators {
    override fun register(context: ComponentActivity, orchestrators: List<Orchestrator>) {
        orchestrators.forEach {
            when (it) {
                is Orchestrator.PlansOrchestrator -> plansOrchestrator.register(context)
            }
        }
    }

    override suspend fun start(orchestrator: Orchestrator) {
        when (orchestrator) {
            is Orchestrator.PlansOrchestrator -> {
                val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return
                plansOrchestrator.startUpgradeWorkflow(userId)
            }
        }
    }
}
