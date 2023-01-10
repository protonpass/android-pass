package proton.android.pass.data.impl.usecases

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import proton.pass.domain.entity.PackageName
import javax.inject.Inject

class GetAppNameFromPackageNameImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : GetAppNameFromPackageName {

    override fun invoke(packageName: PackageName): String {
        val packageManager = context.packageManager
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName.packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            packageManager.getApplicationInfo(packageName.packageName, PackageManager.GET_META_DATA)
        }

        return packageManager.getApplicationLabel(appInfo).toString()
    }
}
