package proton.android.pass.account.fakes

import androidx.activity.ComponentActivity
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import javax.inject.Inject

class TestAccountOrchestrators @Inject constructor() : AccountOrchestrators {
    override fun register(context: ComponentActivity, orchestrators: List<Orchestrator>) {

    }

    override suspend fun start(orchestrator: Orchestrator) {

    }
}
