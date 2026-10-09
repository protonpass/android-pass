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
import kotlinx.datetime.Instant
import org.junit.Test
import proton.android.pass.commonrust.impl.ItemTestData.libraryNote
import proton.android.pass.commonrust.impl.ItemTestData.libraryNoteWithPackage
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.types.AutofillUrl
import proton.android.pass.types.AutofillUrlMode
import proton.android.pass.types.ItemContent
import proton.android.pass.types.ItemData
import proton.android.pass.types.LoginItem

internal class LibraryItemDataUpdatesTest {

    @Test
    fun `hasPackageName detects if item is not present`() {
        val source = libraryNoteWithPackage(null)

        assertThat(source.hasPackageName(PackageName("not.present"))).isFalse()
    }

    @Test
    fun `hasPackageName detects if item is present`() {
        val source = libraryNoteWithPackage("abc.def")

        assertThat(source.hasPackageName(PackageName("abc.def"))).isTrue()
    }

    @Test
    fun `hasPackageName is false when platform specific is absent`() {
        assertThat(libraryNote(platformSpecific = null).hasPackageName(PackageName("abc.def"))).isFalse()
    }

    @Test
    fun `addPackageInfo should add a new app`() {
        val source = libraryNoteWithPackage("original.package")
        val newPackageInfo = PackageInfo(PackageName("new.package.name"), AppName("New app"))

        val updated = source.withPackageInfo(newPackageInfo)

        val allowedApps = updated.platformSpecific?.android?.allowedApps.orEmpty()
        assertThat(allowedApps.map { it.packageName }).containsExactly("original.package", "new.package.name").inOrder()
        assertThat(allowedApps[1].appName).isEqualTo("New app")
        assertThat(source.platformSpecific?.android?.allowedApps).hasSize(1)
    }

    @Test
    fun `addPackageInfo on item without platform specific creates it`() {
        val updated = libraryNote(platformSpecific = null)
            .withPackageInfo(PackageInfo(PackageName("new.package.name"), AppName("New app")))

        assertThat(updated.platformSpecific?.android?.allowedApps?.map { it.packageName })
            .containsExactly("new.package.name")
    }

    @Test
    fun `adding package association preserves its signing hashes`() {
        val packageInfo = PackageInfo(
            packageName = PackageName("new.package.name"),
            appName = AppName("New app"),
            hashes = setOf("certificate-a", "certificate-b")
        )

        val updated = libraryNoteWithPackage(null).withPackageInfo(packageInfo)

        assertThat(updated.platformSpecific?.android?.allowedApps?.last()?.hashes)
            .containsExactly("certificate-a", "certificate-b")
    }

    @Test
    fun `adding signing hashes upgrades an existing package association`() {
        val packageInfo = PackageInfo(PackageName("existing.package"), AppName("Existing app"), setOf("certificate-a"))

        val updated = libraryNoteWithPackage("existing.package").withPackageInfo(packageInfo)

        val allowedApps = updated.platformSpecific?.android?.allowedApps.orEmpty()
        assertThat(allowedApps).hasSize(1)
        assertThat(allowedApps.single().hashes).containsExactly("certificate-a")
    }

    @Test
    fun `adding existing signing hashes merges without duplicates`() {
        val packageInfo = PackageInfo(
            packageName = PackageName("existing.package"),
            appName = AppName("Existing app"),
            hashes = setOf("certificate-a", "certificate-b")
        )
        val source = libraryNoteWithPackage("existing.package", hashes = listOf("certificate-a"))

        val updated = source.withPackageInfo(packageInfo)

        assertThat(updated.platformSpecific?.android?.allowedApps?.single()?.hashes)
            .containsExactly("certificate-a", "certificate-b")
            .inOrder()
    }

    @Test
    fun `addPackageInfoIfChanged returns null when nothing changes`() {
        val source = libraryNoteWithPackage("existing.package", hashes = listOf("certificate-a"))
        val packageInfo = PackageInfo(PackageName("existing.package"), AppName("Existing app"), setOf("certificate-a"))

        assertThat(source.withPackageInfoIfChanged(packageInfo)).isNull()
    }

    @Test
    fun `addPackageInfoIfChanged returns updated item when package is new`() {
        val source = libraryNoteWithPackage("existing.package")
        val packageInfo = PackageInfo(PackageName("other.package"), AppName("Other"))

        val updated = source.withPackageInfoIfChanged(packageInfo)

        assertThat(updated?.platformSpecific?.android?.allowedApps?.map { it.packageName })
            .containsExactly("existing.package", "other.package")
    }

    @Test
    fun `addPackageInfo keeps custom icon and uuid`() {
        val source = libraryNote(customIcon = byteArrayOf(9), itemUuid = "keep")

        val updated = source.withPackageInfo(PackageInfo(PackageName("p"), AppName("a")))

        assertThat(updated.customIcon).isEqualTo(source.customIcon)
        assertThat(updated.itemUuid).isEqualTo("keep")
    }

    @Test
    fun `addUrl in basic mode only appends to urls`() {
        val source = login(urls = listOf("https://a.com"), autofillUrls = emptyList())

        val updated = source.withUrl("https://b.com", isAutofillUrlAdvancedModesEnabled = false)

        val loginItem = (updated.content as ItemContent.Login).v1
        assertThat(loginItem.urls).containsExactly("https://a.com", "https://b.com").inOrder()
        assertThat(loginItem.autofillUrls).isEmpty()
    }

    @Test
    fun `addUrl in advanced mode seeds autofill urls from existing urls`() {
        val source = login(urls = listOf("https://a.com", " "), autofillUrls = emptyList())

        val updated = source.withUrl("https://b.com", isAutofillUrlAdvancedModesEnabled = true)

        val loginItem = (updated.content as ItemContent.Login).v1
        assertThat(loginItem.urls).containsExactly("https://a.com", " ", "https://b.com").inOrder()
        assertThat(loginItem.autofillUrls).containsExactly(
            AutofillUrl(url = "https://a.com", mode = AutofillUrlMode.DEFAULT),
            AutofillUrl(url = "https://b.com", mode = AutofillUrlMode.DEFAULT)
        ).inOrder()
    }

    @Test
    fun `addUrl in advanced mode keeps existing autofill urls`() {
        val existing = AutofillUrl(url = "https://a.com", mode = AutofillUrlMode.EXACT)
        val source = login(urls = listOf("https://a.com"), autofillUrls = listOf(existing))

        val updated = source.withUrl("https://b.com", isAutofillUrlAdvancedModesEnabled = true)

        val loginItem = (updated.content as ItemContent.Login).v1
        assertThat(loginItem.autofillUrls).containsExactly(
            existing,
            AutofillUrl(url = "https://b.com", mode = AutofillUrlMode.DEFAULT)
        ).inOrder()
    }

    @Test
    fun `addUrl on non login item returns it unchanged`() {
        val source = libraryNote()

        assertThat(source.withUrl("https://b.com", true)).isEqualTo(source)
    }

    @Test
    fun `addPasskey appends the passkey to the login`() {
        val source = login(urls = emptyList(), autofillUrls = emptyList())

        val updated = source.withPasskey(domainPasskey())

        val passkeys = (updated.content as ItemContent.Login).v1.passkeys
        assertThat(passkeys).hasSize(1)
        assertThat(passkeys.single().keyId).isEqualTo("key-id")
        assertThat(passkeys.single().rpId).isEqualTo("rp.com")
        assertThat(passkeys.single().createTime).isEqualTo(1_700_000_000u)
        assertThat(passkeys.single().userHandle).isEmpty()
        assertThat(passkeys.single().creationData).isNull()
    }

    @Test
    fun `addPasskey keeps existing passkeys`() {
        val once = login(urls = emptyList(), autofillUrls = emptyList()).withPasskey(domainPasskey())

        val twice = once.withPasskey(domainPasskey().copy(id = PasskeyId("second")))

        assertThat((twice.content as ItemContent.Login).v1.passkeys.map { it.keyId })
            .containsExactly("key-id", "second")
            .inOrder()
    }

    private fun login(urls: List<String>, autofillUrls: List<AutofillUrl>): ItemData = libraryNote().copy(
        content = ItemContent.Login(
            LoginItem(
                email = "",
                username = "",
                password = "",
                urls = urls,
                totpUri = "",
                passkeys = emptyList(),
                autofillUrls = autofillUrls
            )
        )
    )

    private fun domainPasskey() = Passkey(
        id = PasskeyId("key-id"),
        domain = "rp.com",
        rpId = "rp.com",
        rpName = "RP",
        userName = "user",
        userDisplayName = "User",
        userId = ByteArrayWrapper(byteArrayOf(1)),
        note = "",
        createTime = Instant.fromEpochSeconds(1_700_000_000L),
        contents = ByteArrayWrapper(byteArrayOf(2)),
        userHandle = null,
        credentialId = ByteArrayWrapper(byteArrayOf(3)),
        creationData = null
    )
}
