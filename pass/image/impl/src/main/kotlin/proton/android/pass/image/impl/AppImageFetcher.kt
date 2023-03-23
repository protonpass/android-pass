package proton.android.pass.image.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.crypto.api.HashUtils
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.entity.PackageName
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AppImageFetcherFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock
) : Fetcher.Factory<PackageName> {
    override fun create(data: PackageName, options: Options, imageLoader: ImageLoader): Fetcher =
        AppImageFetcher(context, clock, data)
}

class AppImageFetcher(
    private val context: Context,
    private val clock: Clock,
    private val packageName: PackageName
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        performFetch()
    }

    @Suppress("ReturnCount")
    private fun performFetch(): FetchResult? {
        when (val res = cachedFile()) {
            CacheResult.NotExists -> return null // We are sure it does not exist
            CacheResult.Miss -> {} // We don't know if it exists or not
            is CacheResult.Exists -> { // It exists and we have it, return it
                val buff = okio.Buffer().write(res.file.readBytes())
                return SourceResult(
                    source = ImageSource(buff, context),
                    mimeType = PNG_MIME_TYPE,
                    dataSource = DataSource.DISK
                )
            }
        }

        PassLogger.d(TAG, "Could not find cached icon for ${packageName.value}")
        val appIcon = AndroidUtils.getApplicationIcon(context, packageName.value)

        val hash = getHash()
        val imageData = when (appIcon) {
            None -> {
                storeEmptyResult(hash)
                PassLogger.d(TAG, "Persisted Empty Result for ${packageName.value}")
                return null
            }
            is Some -> appIcon.value
        }

        persistToCache(hash, imageData)
        PassLogger.d(TAG, "Persisted to cache icon for ${packageName.value}")

        return DrawableResult(
            drawable = imageData,
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    private fun storeEmptyResult(hash: String) {
        val filename = "$hash.$NOT_EXISTS_EXTENSION"
        val cacheFile = File(CacheUtils.cacheDir(context), filename)
        cacheFile.createNewFile()
    }

    private fun persistToCache(hash: String, drawable: Drawable) {
        val asBitmap = drawable.toBitmap(width = BITMAP_WIDTH, height = BITMAP_HEIGHT, config = Bitmap.Config.ARGB_8888)

        val filename = "$hash.$PNG_EXTENSION"
        val cacheFile = File(CacheUtils.cacheDir(context), filename)
        cacheFile.createNewFile()

        val outStream = FileOutputStream(cacheFile)
        asBitmap.compress(Bitmap.CompressFormat.PNG, WEBP_QUALITY, outStream)
        outStream.close()
    }

    private fun cachedFile(): CacheResult {
        val hash = HashUtils.sha256(packageName.value)
        val pngFile = File(CacheUtils.cacheDir(context), "$hash.$PNG_EXTENSION")
        val notExistsFile = File(CacheUtils.cacheDir(context), "$hash.$NOT_EXISTS_EXTENSION")

        return if (pngFile.exists()) {
            PassLogger.d(TAG, "Found cached png icon for ${packageName.value}")
            handleCachedFile(pngFile, CacheResult.Exists(pngFile))
        } else if (notExistsFile.exists()) {
            PassLogger.d(TAG, "Found notexists file for ${packageName.value}")
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

    private fun getHash(): String = HashUtils.sha256(packageName.value)

    private sealed interface CacheResult {
        data class Exists(val file: File) : CacheResult
        object NotExists : CacheResult
        object Miss : CacheResult
    }

    companion object {
        private const val BITMAP_WIDTH = 512
        private const val BITMAP_HEIGHT = 512
        private const val WEBP_QUALITY = 100

        private const val PNG_EXTENSION = "png"
        private const val NOT_EXISTS_EXTENSION = "noexist"

        private const val PNG_MIME_TYPE = "image/png"

        private const val CACHE_EXPIRATION_DAYS = 14

        private const val TAG = "AppImageFetcher"
    }
}
