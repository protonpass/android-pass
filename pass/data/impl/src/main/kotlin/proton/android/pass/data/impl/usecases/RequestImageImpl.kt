package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ImageResponseResult
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.data.impl.remote.RemoteImageFetcher
import javax.inject.Inject

class RequestImageImpl @Inject constructor(
    private val fetcher: RemoteImageFetcher,
    private val accountManager: AccountManager
) : RequestImage {
    override fun invoke(domain: String): Flow<ImageResponseResult> = flow {
        val parsed = UrlSanitizer.getDomain(domain).getOrThrow()
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        fetcher.fetchFavicon(userId, "no-reply@$parsed")
            .catch { emit(ImageResponseResult.Error(it)) }
            .collect {
                if (it == null) {
                    emit(ImageResponseResult.Empty)
                } else {
                    emit(ImageResponseResult.Data(it.content, it.mimeType))
                }
            }
    }
}
