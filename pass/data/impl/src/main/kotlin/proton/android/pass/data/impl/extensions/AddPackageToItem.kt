/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.extensions

import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

fun ItemV1.Item.hasPackageName(packageName: PackageName): Boolean =
    platformSpecific.android.allowedAppsList.any { it.packageName == packageName.value }

fun ItemV1.Item.with(packageInfo: PackageInfo): ItemV1.Item {
    val allowedApps = platformSpecific.android.allowedAppsList.toMutableList()
    allowedApps.add(
        ItemV1.AllowedAndroidApp.newBuilder()
            .setAppName(packageInfo.appName.value)
            .setPackageName(packageInfo.packageName.value)
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
