package proton.android.pass.image.impl

import android.content.Context
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import proton.android.pass.crypto.api.HashUtils
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ImageResponse
import proton.android.pass.data.api.usecases.RequestImage
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageFetcherFactory @Inject constructor(
    private val requestImage: RequestImage,
    @ApplicationContext private val context: Context,
) : Fetcher.Factory<Uri> {
    override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher =
        ImageFetcher(requestImage, context, data)
}

class ImageFetcher(
    private val requestImage: RequestImage,
    private val context: Context,
    private val uri: Uri,
) : Fetcher {

    @Suppress("ReturnCount")
    override suspend fun fetch(): FetchResult? {
        val domain = getDomain() ?: return null

        val cached = getCached(domain)
        if (cached != null) {
            return cached
        }

        PassLogger.d(TAG, "Could not find cached icon for $domain")
        val res = requestImage.invoke(domain).first()
        if (res == null) {
            PassLogger.d(TAG, "Could not retrieve icon for $domain")
            return null
        }

        persistToCache(domain, res)
        PassLogger.d(TAG, "Persisted to cache icon for $domain")

        val buff = okio.Buffer().write(res.content)
        return SourceResult(
            source = ImageSource(buff, context),
            mimeType = res.mimeType,
            dataSource = DataSource.NETWORK
        )
    }

    private fun persistToCache(domain: String, response: ImageResponse) {
        val hashed = HashUtils.sha256(domain)
        val filename = if (response.mimeType == SVG_MIME_TYPE) {
            "$hashed.$SVG_EXTENSION"
        } else {
            "$hashed.$WEBP_EXTENSION"
        }

        val cacheFile = File(cacheDir(), filename)
        cacheFile.createNewFile()
        cacheFile.writeBytes(response.content)
    }

    private fun getCached(domain: String): FetchResult? {
        val (cachedFile, mimeType) = cachedFile(domain) ?: return null
        val buff = okio.Buffer().write(cachedFile.readBytes())
        return SourceResult(
            source = ImageSource(buff, context),
            mimeType = mimeType.mimeType,
            dataSource = DataSource.DISK
        )
    }

    private fun cachedFile(domain: String): Pair<File, MimeType>? {
        val hashed = HashUtils.sha256(domain)
        val webpFile = File(cacheDir(), "$hashed.$WEBP_EXTENSION")
        val svgFile = File(cacheDir(), "$hashed.$SVG_EXTENSION")

        return if (webpFile.exists()) {
            PassLogger.d(TAG, "Found cached webp icon for $domain")
            webpFile to MimeType.Webp
        } else if (svgFile.exists()) {
            PassLogger.d(TAG, "Found svg icon for $domain")
            svgFile to MimeType.Svg
        } else {
            null
        }
    }

    private fun cacheDir(): File {
        val iconCacheDir = File(context.cacheDir, ICON_CACHE_DIR_NAME)
        if (!iconCacheDir.exists()) {
            iconCacheDir.mkdirs()
        }

        return iconCacheDir
    }

    private fun getDomain(): String? {
        val uriHost = uri.host
        if (uriHost != null) return uriHost

        return UrlSanitizer.getDomain(uri.toString()).fold(
            onSuccess = { it },
            onFailure = { null }
        )
    }

    private enum class MimeType(val mimeType: String) {
        Webp(WEBP_MIME_TYPE),
        Svg(SVG_MIME_TYPE)
    }

    companion object {
        private const val ICON_CACHE_DIR_NAME = "icon"
        private const val WEBP_EXTENSION = ".webp"
        private const val SVG_EXTENSION = ".svg"

        private const val SVG_MIME_TYPE = "image/svg+xml"
        private const val WEBP_MIME_TYPE = "image/webp"

        private const val TAG = "ImageFetcher"
    }
}
