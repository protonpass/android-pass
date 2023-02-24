package proton.android.pass.image.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface ClearIconCache {
    suspend operator fun invoke()
}

class ClearIconCacheImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClearIconCache {
    override suspend fun invoke() = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Removing icon cache")
        val cacheDir = CacheUtils.cacheDir(context)
        if (cacheDir.deleteRecursively()) {
            PassLogger.i(TAG, "Removed icon cache")
        } else {
            PassLogger.w(TAG, "Could not remove icon cache")
        }
    }

    companion object {
        private const val TAG = "ClearIconCacheImpl"
    }

}
