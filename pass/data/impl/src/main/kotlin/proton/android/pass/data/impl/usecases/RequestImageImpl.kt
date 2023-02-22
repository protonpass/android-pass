package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import org.apache.commons.codec.binary.Base64
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RequestImageImpl @Inject constructor(
    private val fetcher: RemoteImageFetcher,
    private val accountManager: AccountManager
) : RequestImage {
    override fun invoke(domain: String): Flow<ByteArray> = flow {
        val parsed = UrlSanitizer.getDomain(domain).fold(
            onSuccess = { it },
            onFailure = {
                throw IllegalStateException("Could not get domain")
            }
        )

        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        val res = fetcher.fetchFavicon(userId, "something@$parsed").first()
        PassLogger.i("Random", Base64.encodeBase64String(res))
        emit(res)
    }
}
