package proton.android.pass.account.fakes.network

import me.proton.core.network.domain.ApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeApiClient @Inject constructor() : ApiClient {
    override val appVersionHeader: String = ""
    override val userAgent: String = ""
    override val enableDebugLogging: Boolean = false

    override suspend fun shouldUseDoh(): Boolean = false
    override fun forceUpdate(errorMessage: String) = Unit
}
