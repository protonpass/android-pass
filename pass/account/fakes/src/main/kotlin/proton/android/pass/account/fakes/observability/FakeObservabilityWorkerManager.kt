package proton.android.pass.account.fakes.observability

import me.proton.core.observability.domain.ObservabilityWorkerManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Singleton
class FakeObservabilityWorkerManager @Inject constructor() : ObservabilityWorkerManager {
    override fun cancel() = Unit
    override fun enqueueOrKeep(delay: Duration) = Unit
}
