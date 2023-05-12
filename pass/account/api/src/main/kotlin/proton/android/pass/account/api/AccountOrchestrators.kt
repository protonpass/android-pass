package proton.android.pass.account.api

import androidx.activity.ComponentActivity

interface AccountOrchestrators {
    fun register(context: ComponentActivity, orchestrators: List<Orchestrator>)
    suspend fun start(orchestrator: Orchestrator)
}

sealed class Orchestrator {
    object PlansOrchestrator : Orchestrator()
}
