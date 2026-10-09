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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonrust.impl.ItemTestData.concealed
import proton.android.pass.commonrust.impl.ItemTestData.domainNote
import proton.android.pass.commonrust.impl.ItemTestData.encryptionContext
import proton.android.pass.commonrust.impl.ItemTestData.libraryNote
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.types.AllowedAndroidApp
import proton.android.pass.types.AndroidSpecific
import proton.android.pass.types.ItemContent
import proton.android.pass.types.ItemData
import proton.android.pass.types.PlatformSpecific

internal class ItemOverlayTest {

    private val icon = byteArrayOf(1, 2, 3)
    private val libraryPlatformSpecific = PlatformSpecific(
        android = AndroidSpecific(
            allowedApps = listOf(AllowedAndroidApp(packageName = "some.app", hashes = listOf("h"), appName = "App"))
        )
    )

    @Test
    fun `overlay keeps custom icon and item uuid of the existing item`() {
        val existing = libraryNote(customIcon = icon, itemUuid = "existing-uuid")

        val result = domainNote().toLibraryItemData(existing.itemUuid, existing, encryptionContext)

        assertThat(result.itemUuid).isEqualTo("existing-uuid")
        assertThat(result.customIcon).isEqualTo(icon)
        assertThat(result.title).isEqualTo("Updated title")
    }

    @Test
    fun `overlay keeps platform specific of non login items`() {
        val existing = libraryNote(platformSpecific = libraryPlatformSpecific)

        val result = domainNote().toLibraryItemData(existing.itemUuid, existing, encryptionContext)

        assertThat(result.platformSpecific).isEqualTo(libraryPlatformSpecific)
    }

    @Test
    fun `overlay keeps absent options as none for non login items`() {
        val existing = libraryNote(platformSpecific = null, customIcon = null)

        val result = domainNote().toLibraryItemData(existing.itemUuid, existing, encryptionContext)

        assertThat(result.platformSpecific).isNull()
        assertThat(result.customIcon).isNull()
    }

    @Test
    fun `overlay keeps absent platform specific for login without packages`() {
        val existing = existingItem(platformSpecific = null)

        val result = domainLogin(packageInfoSet = emptySet()).toLibraryItemData(
            itemUuid = existing.itemUuid,
            existing = existing,
            encryptionContext = encryptionContext
        )

        assertThat(result.platformSpecific).isNull()
    }

    @Test
    fun `overlay replaces login packages with the domain ones`() {
        val existing = existingItem(platformSpecific = libraryPlatformSpecific)
        val packageInfo = PackageInfo(PackageName("new.app"), AppName("New"), setOf("x", "y"))

        val result = domainLogin(setOf(packageInfo)).toLibraryItemData(
            itemUuid = existing.itemUuid,
            existing = existing,
            encryptionContext = encryptionContext
        )

        assertThat(result.platformSpecific?.android?.allowedApps).containsExactly(
            AllowedAndroidApp(packageName = "new.app", hashes = listOf("x", "y"), appName = "New")
        )
    }

    @Test
    fun `overlay keeps stored hashes of login packages edited without hashes`() {
        val existing = existingItem(platformSpecific = libraryPlatformSpecific)
        val packageInfo = PackageInfo(PackageName("some.app"), AppName("App"))

        val result = domainLogin(setOf(packageInfo)).toLibraryItemData(
            itemUuid = existing.itemUuid,
            existing = existing,
            encryptionContext = encryptionContext
        )

        assertThat(result.platformSpecific?.android?.allowedApps).containsExactly(
            AllowedAndroidApp(packageName = "some.app", hashes = listOf("h"), appName = "App")
        )
    }

    @Test
    fun `overlay merges stored and new hashes of the same login package`() {
        val existing = existingItem(platformSpecific = libraryPlatformSpecific)
        val packageInfo = PackageInfo(PackageName("some.app"), AppName("App"), setOf("h", "n"))

        val result = domainLogin(setOf(packageInfo)).toLibraryItemData(
            itemUuid = existing.itemUuid,
            existing = existing,
            encryptionContext = encryptionContext
        )

        assertThat(result.platformSpecific?.android?.allowedApps).containsExactly(
            AllowedAndroidApp(packageName = "some.app", hashes = listOf("h", "n"), appName = "App")
        )
    }

    @Test
    fun `new item without existing gets empty android platform specific and no icon`() {
        val result = domainNote().toLibraryItemData("uuid", null, encryptionContext)

        assertThat(result.platformSpecific).isEqualTo(PlatformSpecific(AndroidSpecific(emptyList())))
        assertThat(result.customIcon).isNull()
    }

    @Test
    fun `login urls are filtered and secrets are decrypted`() {
        val result = domainLogin(emptySet(), urls = listOf("https://a.com", " ", ""))
            .toLibraryItemData("uuid", null, encryptionContext)

        val login = (result.content as ItemContent.Login).v1
        assertThat(login.urls).containsExactly("https://a.com")
        assertThat(login.password).isEqualTo("password")
        assertThat(login.totpUri).isEqualTo("totp")
    }

    private fun existingItem(platformSpecific: PlatformSpecific?): ItemData =
        libraryNote(platformSpecific = platformSpecific, customIcon = icon)

    private fun domainLogin(packageInfoSet: Set<PackageInfo>, urls: List<String> = emptyList()): ItemContents.Login =
        ItemContents.Login(
            title = "login",
            note = "",
            customFields = emptyList(),
            itemEmail = "a@b.c",
            itemUsername = "user",
            password = concealed("password"),
            urls = urls,
            packageInfoSet = packageInfoSet,
            primaryTotp = concealed("totp"),
            passkeys = emptyList(),
            autofillUrls = emptyList()
        )
}
