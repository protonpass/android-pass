package proton.android.pass.account.fakes.observability

import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeIsObservabilityEnabled @Inject constructor() : IsObservabilityEnabled {
    override suspend fun invoke(): Boolean = false
}
