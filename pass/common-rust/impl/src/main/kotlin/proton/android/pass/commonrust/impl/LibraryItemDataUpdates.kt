/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl

import proton.android.pass.domain.Passkey
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.types.AllowedAndroidApp as LibraryAllowedAndroidApp
import proton.android.pass.types.AndroidSpecific as LibraryAndroidSpecific
import proton.android.pass.types.AutofillUrl as LibraryAutofillUrl
import proton.android.pass.types.AutofillUrlMode as LibraryAutofillUrlMode
import proton.android.pass.types.ItemContent as LibraryItemContent
import proton.android.pass.types.ItemData as LibraryItemData
import proton.android.pass.types.PlatformSpecific as LibraryPlatformSpecific

internal fun LibraryItemData.hasPackageName(packageName: PackageName): Boolean =
    platformSpecific?.android?.allowedApps.orEmpty().any { it.packageName == packageName.value }

internal fun LibraryItemData.withPackageInfo(packageInfo: PackageInfo): LibraryItemData {
    val allowedApps = platformSpecific?.android?.allowedApps.orEmpty().toMutableList()
    val existingIndex = allowedApps.indexOfFirst { it.packageName == packageInfo.packageName.value }
    val existingApp = allowedApps.getOrNull(existingIndex)
    val updatedApp = existingApp
        ?.copy(hashes = (existingApp.hashes + packageInfo.hashes).distinct())
        ?: LibraryAllowedAndroidApp(
            packageName = packageInfo.packageName.value,
            hashes = packageInfo.hashes.toList(),
            appName = packageInfo.appName.value
        )

    if (existingIndex == -1) {
        allowedApps.add(updatedApp)
    } else {
        allowedApps[existingIndex] = updatedApp
    }

    return copy(
        platformSpecific = LibraryPlatformSpecific(android = LibraryAndroidSpecific(allowedApps = allowedApps))
    )
}

internal fun LibraryItemData.withPackageInfoIfChanged(packageInfo: PackageInfo): LibraryItemData? =
    withPackageInfo(packageInfo).takeIf { it != this }

internal fun LibraryItemData.withUrl(url: String, isAutofillUrlAdvancedModesEnabled: Boolean): LibraryItemData {
    val login = content as? LibraryItemContent.Login ?: return this
    val loginItem = login.v1

    val existingAutofillUrls = if (isAutofillUrlAdvancedModesEnabled && loginItem.autofillUrls.isEmpty()) {
        loginItem.urls
            .filter { it.isNotBlank() }
            .map { LibraryAutofillUrl(url = it, mode = LibraryAutofillUrlMode.DEFAULT) }
    } else {
        loginItem.autofillUrls
    }

    val autofillUrls = if (isAutofillUrlAdvancedModesEnabled) {
        existingAutofillUrls + LibraryAutofillUrl(url = url, mode = LibraryAutofillUrlMode.DEFAULT)
    } else {
        existingAutofillUrls
    }

    return copy(
        content = LibraryItemContent.Login(loginItem.copy(urls = loginItem.urls + url, autofillUrls = autofillUrls))
    )
}

internal fun LibraryItemData.withPasskey(passkey: Passkey): LibraryItemData {
    val login = content as? LibraryItemContent.Login ?: return this
    val loginItem = login.v1
    return copy(content = LibraryItemContent.Login(loginItem.copy(passkeys = loginItem.passkeys + passkey.toLibrary())))
}
