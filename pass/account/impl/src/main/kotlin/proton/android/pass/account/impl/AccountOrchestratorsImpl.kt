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
