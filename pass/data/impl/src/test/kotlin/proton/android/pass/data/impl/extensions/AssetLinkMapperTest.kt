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

package proton.android.pass.data.impl.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.data.impl.responses.AssetLinkResponse
import proton.android.pass.data.impl.responses.TargetResponse
import proton.android.pass.domain.assetlink.AssetLink

class AssetLinkMapperTest {

    @Test
    fun `toDomain retains android app target with get login credentials relation`() {
        val response = assetLinkResponse(relation = listOf(GetLoginCredentialsRelation))

        val assetLink = listOf(response).toDomain(Website)

        assertThat(assetLink).isEqualTo(
            AssetLink(
                website = Website,
                packages = setOf(
                    AssetLink.Package(
                        packageName = PackageName,
                        signatures = setOf(Signature)
                    )
                )
            )
        )
    }

    @Test
    fun `toDomain rejects handle all urls only relation`() {
        val response = assetLinkResponse(relation = listOf(HandleAllUrlsRelation))

        assertThat(listOf(response).toDomain(Website).packages).isEmpty()
    }

    @Test
    fun `toDomain rejects unknown relation`() {
        val response = assetLinkResponse(relation = listOf("delegate_permission/common.unknown"))

        assertThat(listOf(response).toDomain(Website).packages).isEmpty()
    }

    @Test
    fun `toDomain rejects empty relation`() {
        val response = assetLinkResponse(relation = emptyList())

        assertThat(listOf(response).toDomain(Website).packages).isEmpty()
    }

    @Test
    fun `toDomain rejects non android app target`() {
        val response = assetLinkResponse(
            relation = listOf(GetLoginCredentialsRelation),
            namespace = "web"
        )

        assertThat(listOf(response).toDomain(Website).packages).isEmpty()
    }

    private fun assetLinkResponse(relation: List<String>, namespace: String = AndroidAppNamespace) = AssetLinkResponse(
        relation = relation,
        target = TargetResponse(
            namespace = namespace,
            packageName = PackageName,
            sha256CertFingerprints = listOf(Signature)
        )
    )

    private companion object {
        const val Website = "https://example.com"
        const val PackageName = "com.example.app"
        const val Signature = "AA:BB:CC"
        const val AndroidAppNamespace = "android_app"
        const val GetLoginCredentialsRelation = "delegate_permission/common.get_login_creds"
        const val HandleAllUrlsRelation = "delegate_permission/common.handle_all_urls"
    }
}
