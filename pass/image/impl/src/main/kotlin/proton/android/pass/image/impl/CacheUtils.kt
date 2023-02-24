package proton.android.pass.image.impl

import android.content.Context
import java.io.File

object CacheUtils {
    private const val ICON_CACHE_DIR_NAME = "icon"

    fun cacheDir(context: Context): File {
        val iconCacheDir = File(context.cacheDir, ICON_CACHE_DIR_NAME)
        if (!iconCacheDir.exists()) {
            iconCacheDir.mkdirs()
        }

        return iconCacheDir
    }
}
