package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
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

        val res = api.get<PasswordManagerApi>(userId).invoke {
            getFavicon(domain)
        }.valueOrThrow

        when (res.code()) {
            HTTP_OK -> {
                val body = checkNotNull(res.body()?.bytes())
                val mimeType = res.headers().get("Content-Type")
                emit(ImageResponse(content = body, mimeType = mimeType))
            }
            HTTP_UNPROCESSABLE_CONTENT -> {
                val pmCode = res.headers().get(HEADER_PM_CODE)
                if (pmCode == PM_CODE_NOT_TRUSTED) {
                    emit(null)
                } else {
                    throw ApiException(
                        ApiResult.Error.Http(
                            res.code(),
                            "Missing $HEADER_PM_CODE in $HTTP_UNPROCESSABLE_CONTENT response"
                        )
                    )
                }
            }
            else -> {
                throw ApiException(
                    ApiResult.Error.Http(
                        res.code(),
                        "Unknown response code ${res.code()}"
                    )
                )
            }
        }


    }

    companion object {
        private const val HTTP_OK = 200
        private const val HTTP_UNPROCESSABLE_CONTENT = 422
        private const val HEADER_PM_CODE = "X-Pm-Code"
        private const val PM_CODE_NOT_TRUSTED = "2011"
    }
}
