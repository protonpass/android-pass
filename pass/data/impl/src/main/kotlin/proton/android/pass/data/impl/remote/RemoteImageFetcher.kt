package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import javax.inject.Inject

interface RemoteImageFetcher {
    fun fetchFavicon(userId: UserId, domain: String): Flow<ByteArray>
}

class RemoteImageFetcherImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteImageFetcher {
    override fun fetchFavicon(userId: UserId, domain: String): Flow<ByteArray> = flow {

        val res = api.get<PasswordManagerApi>(userId).invoke {
            getFavicon(domain)
        }.valueOrThrow

        if (res.code() != HTTP_OK) {
            throw IllegalStateException("Should return 200")
        }

        val body = res.body()?.bytes() ?: throw IllegalStateException("Cannot read body")
        emit(body)
    }

    companion object {
        private const val HTTP_OK = 200
    }
}
