package me.proton.android.pass.data.impl.extensions

import me.proton.pass.domain.entity.PackageName
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
        .setPlatformSpecific(
            platformSpecific.toBuilder()
                .setAndroid(
                    platformSpecific.android.toBuilder()
                        .clearAllowedApps()
                        .addAllAllowedApps(allowedApps)
                        .build()
                )
                .build()
        )
        .build()
}

fun ItemV1.Item.withUrl(url: String): ItemV1.Item {
    val websites = content.login.urlsList.toMutableList()
    websites.add(url)
    return this.toBuilder()
        .setContent(
            content.toBuilder()
                .setLogin(
                    content.login.toBuilder()
                        .addUrls(url)
                        .build()
                )
                .build()
        )
        .build()
}
