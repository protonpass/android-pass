package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.ProtonErrorException
import proton.android.pass.data.api.usecases.ImageResponse
import proton.android.pass.data.impl.api.PasswordManagerApi
import javax.inject.Inject

interface RemoteImageFetcher {
    fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?>
}

class RemoteImageFetcherImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteImageFetcher {
    override fun fetchFavicon(userId: UserId, domain: String): Flow<ImageResponse?> = flow {
        api.get<PasswordManagerApi>(userId).invoke {
            try {
                val res = getFavicon(domain)
                val body = checkNotNull(res.body()?.bytes())
                val mimeType = res.headers().get("Content-Type")
                emit(ImageResponse(content = body, mimeType = mimeType))
            } catch (e: ProtonErrorException) {
                if (e.response.code == HTTP_UNPROCESSABLE_CONTENT && e.protonData.code == PROTON_CODE_NOT_TRUSTED) {
                    emit(null)
                    return@invoke
                }
                throw e
            }
        }
    }

    companion object {
        private const val HTTP_UNPROCESSABLE_CONTENT = 422
        private const val PROTON_CODE_NOT_TRUSTED = 2011
    }
}
