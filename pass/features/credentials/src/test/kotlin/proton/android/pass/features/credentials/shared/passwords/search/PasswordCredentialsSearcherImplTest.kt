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
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing

internal class PasswordCredentialsSearcherImplTest {

    @Test
    internal fun `static password BeginGet searcher dependency graph excludes live DAL verifier`() {
        // This intentionally checks only the static dependency graph. The selection-flow tests
        // cover that no live verification occurs before explicit user selection.
        val constructorDependencies = PasswordCredentialsSearcherImpl::class.java
            .declaredConstructors
            .single()
            .parameterTypes
            .toSet()
        val fieldDependencies = PasswordCredentialsSearcherImpl::class.java
            .declaredFields
            .map { it.type }
            .toSet()

        assertThat(constructorDependencies).doesNotContain(VerifyDigitalAssetLinksForCredentialSharing::class.java)
        assertThat(fieldDependencies).doesNotContain(VerifyDigitalAssetLinksForCredentialSharing::class.java)
    }
}
