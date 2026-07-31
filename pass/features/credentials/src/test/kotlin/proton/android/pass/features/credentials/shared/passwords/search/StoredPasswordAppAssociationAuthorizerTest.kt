/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.features.credentials.shared.passwords.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

internal class StoredPasswordAppAssociationAuthorizerTest {

    private val authorizer = StoredPasswordAppAssociationAuthorizer()

    @Test
    fun `authorizes an exact package and signing hash match`() {
        val isAuthorized = authorizer(
            caller = PasswordCallerContext.Native(
                packageName = "com.example.app",
                certificateFingerprints = setOf("certificate-a")
            ),
            associations = setOf(
                PackageInfo(
                    packageName = PackageName("com.example.app"),
                    appName = AppName("Example"),
                    hashes = setOf("certificate-a")
                )
            )
        )

        assertThat(isAuthorized).isTrue()
    }

    @Test
    fun `rejects a legacy package-only association`() {
        val isAuthorized = authorizer(
            caller = PasswordCallerContext.Native(
                packageName = "com.example.app",
                certificateFingerprints = setOf("certificate-a")
            ),
            associations = setOf(
                PackageInfo(PackageName("com.example.app"), AppName("Example"))
            )
        )

        assertThat(isAuthorized).isFalse()
    }

    @Test
    fun `rejects an association for a different signing hash`() {
        val isAuthorized = authorizer(
            caller = PasswordCallerContext.Native(
                packageName = "com.example.app",
                certificateFingerprints = setOf("certificate-b")
            ),
            associations = setOf(
                PackageInfo(
                    packageName = PackageName("com.example.app"),
                    appName = AppName("Example"),
                    hashes = setOf("certificate-a")
                )
            )
        )

        assertThat(isAuthorized).isFalse()
    }
}
