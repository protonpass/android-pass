package proton.android.pass.image.impl

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.crypto.api.HashUtils
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ImageResponseResult
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.image.impl.CacheUtils.cacheDir
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.pass.domain.WebsiteUrl
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class RemoteImageFetcherFactory @Inject constructor(
    private val requestImage: RequestImage,
    @ApplicationContext private val context: Context,
    private val clock: Clock,
    private val userPreferencesRepository: UserPreferencesRepository
) : Fetcher.Factory<WebsiteUrl> {
    override fun create(data: WebsiteUrl, options: Options, imageLoader: ImageLoader): Fetcher {
        val canUse = runBlocking { userPreferencesRepository.getUseFaviconsPreference().first() }
        return if (canUse == UseFaviconsPreference.Enabled) {
            RemoteImageFetcher(requestImage, context, clock, data)
        } else {
            NoOpFetcher()
        }
    }
}

class RemoteImageFetcher(
    private val requestImage: RequestImage,
    private val context: Context,
    private val clock: Clock,
    private val webs: WebsiteUrl
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        performFetch()
    }

    @Suppress("ReturnCount")
    private suspend fun performFetch(): FetchResult? {
        val domain = getDomain() ?: return null

        when (val res = cachedFile(domain)) {
            CacheResult.NotExists -> return null // We are sure it does not exist
            CacheResult.Miss -> {} // We don't know if it exists or not
            is CacheResult.Exists -> { // It exists and we have it, return it
                val buff = okio.Buffer().write(res.file.readBytes())
                return SourceResult(
                    source = ImageSource(buff, context),
                    mimeType = res.mimeType.mimeType,
                    dataSource = DataSource.DISK
                )
            }
        }

        PassLogger.d(TAG, "Could not find cached icon for $domain")
        val res = requestImage.invoke(domain).first()
        val imageData = when (res) {
            is ImageResponseResult.Error -> {
                PassLogger.w(TAG, res.throwable, "Error fetching the image")
                return null
            }
            is ImageResponseResult.Empty -> {
                storeEmptyResult(domain)
                return null
            }
            is ImageResponseResult.Data -> res
        }

        persistToCache(domain, imageData)
        PassLogger.d(TAG, "Persisted to cache icon for $domain")

        val buff = okio.Buffer().write(imageData.content)
        return SourceResult(
            source = ImageSource(buff, context),
            mimeType = res.mimeType,
            dataSource = DataSource.NETWORK
        )
    }

    private fun storeEmptyResult(domain: String) {
        val hashed = HashUtils.sha256(domain)
        val filename = "$hashed.$NOT_EXISTS_EXTENSION"
        val cacheFile = File(cacheDir(context), filename)
        cacheFile.createNewFile()
    }

    private fun persistToCache(domain: String, response: ImageResponseResult.Data) {
        val hashed = HashUtils.sha256(domain)
        val filename = if (response.mimeType == SVG_MIME_TYPE) {
            "$hashed.$SVG_EXTENSION"
        } else {
            "$hashed.$WEBP_EXTENSION"
        }

        val cacheFile = File(cacheDir(context), filename)
        cacheFile.createNewFile()
        cacheFile.writeBytes(response.content)
    }

    private fun cachedFile(domain: String): CacheResult {
        val hashed = HashUtils.sha256(domain)
        val webpFile = File(cacheDir(context), "$hashed.$WEBP_EXTENSION")
        val svgFile = File(cacheDir(context), "$hashed.$SVG_EXTENSION")
        val notExistsFile = File(cacheDir(context), "$hashed.$NOT_EXISTS_EXTENSION")

        return if (webpFile.exists()) {
            PassLogger.d(TAG, "Found cached webp icon for $domain")
            handleCachedFile(webpFile, CacheResult.Exists(webpFile, MimeType.Webp))
        } else if (svgFile.exists()) {
            PassLogger.d(TAG, "Found svg icon for $domain")
            handleCachedFile(svgFile, CacheResult.Exists(svgFile, MimeType.Svg))
        } else if (notExistsFile.exists()) {
            PassLogger.d(TAG, "Found notexists file for $domain")
            handleCachedFile(notExistsFile, CacheResult.NotExists)
        } else {
            CacheResult.Miss
        }
    }

    private fun handleCachedFile(file: File, result: CacheResult): CacheResult =
        if (isFileValid(file)) {
            result
        } else {
            file.delete()
            CacheResult.Miss
        }

    @Suppress("MagicNumber")
    private fun isFileValid(file: File): Boolean {
        val lastModified = Instant.fromEpochMilliseconds(file.lastModified())
        val elapsed = clock.now().minus(lastModified)

        val jitter = Random.nextInt(1, 5)

        return elapsed.inWholeDays < CACHE_EXPIRATION_DAYS + jitter
    }

    private fun getDomain(): String? {
        return UrlSanitizer.getDomain(webs.url).fold(
            onSuccess = { it },
            onFailure = { null }
        )
    }

    private sealed interface CacheResult {
        data class Exists(val file: File, val mimeType: MimeType) : CacheResult
        object NotExists : CacheResult
        object Miss : CacheResult
    }

    private enum class MimeType(val mimeType: String) {
        Webp(WEBP_MIME_TYPE),
        Svg(SVG_MIME_TYPE)
    }

    companion object {
        private const val WEBP_EXTENSION = "webp"
        private const val SVG_EXTENSION = "svg"
        private const val NOT_EXISTS_EXTENSION = "noexist"

        private const val SVG_MIME_TYPE = "image/svg+xml"
        private const val WEBP_MIME_TYPE = "image/webp"

        private const val CACHE_EXPIRATION_DAYS = 14

        private const val TAG = "RemoteImageFetcher"
    }
}
