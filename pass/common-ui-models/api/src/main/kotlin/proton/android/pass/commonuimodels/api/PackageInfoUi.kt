package proton.android.pass.commonuimodels.api

import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName

data class PackageInfoUi(
    val packageName: String,
    val appName: String
) {
    constructor(packageInfo: PackageInfo) : this(
        packageInfo.packageName.value,
        packageInfo.appName.value
    )

    fun toPackageInfo(): PackageInfo = PackageInfo(
        packageName = PackageName(value = packageName),
        appName = AppName(value = appName)
    )
}
