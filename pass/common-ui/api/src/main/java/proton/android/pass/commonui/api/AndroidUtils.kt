package proton.android.pass.commonui.api

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

object AndroidUtils {

    const val TAG = "AndroidUtils"

    @Suppress("DEPRECATION")
    fun getApplicationName(context: Context, packageName: String): Option<String> =
        try {
            val packageManager = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            }
            packageManager.getApplicationLabel(appInfo).toString().toOption()
        } catch (e: PackageManager.NameNotFoundException) {
            PassLogger.d(TAG, e, "Package name not found")
            None
        }

    fun getApplicationIcon(context: Context, packageName: String): Option<Drawable> =
        try {
            context.packageManager.getApplicationIcon(packageName).toOption()
        } catch (e: PackageManager.NameNotFoundException) {
            PassLogger.d(TAG, e, "Package name not found")
            None
        }

}
