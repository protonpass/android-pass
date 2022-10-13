package me.proton.core.pass.data.extensions

import me.proton.core.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

fun ItemV1.Item.hasPackageName(packageName: PackageName): Boolean =
     platformSpecific.android.allowedAppsList.any { it.packageName == packageName.packageName }

fun ItemV1.Item.with(packageName: PackageName): ItemV1.Item {
    val allowedApps = platformSpecific.android.allowedAppsList.toMutableList()
    allowedApps.add(
        ItemV1.AllowedAndroidApp.newBuilder()
            .setPackageName(packageName.packageName)
            .build()
    )

    return this.toBuilder()
        .setPlatformSpecific(platformSpecific.toBuilder()
            .setAndroid(platformSpecific.android.toBuilder()
                .clearAllowedApps()
                .addAllAllowedApps(allowedApps)
                .build())
            .build())
        .build()
}
