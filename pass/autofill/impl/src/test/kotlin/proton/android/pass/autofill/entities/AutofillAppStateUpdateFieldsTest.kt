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

package proton.android.pass.autofill.entities

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.TestAutofillId
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

class AutofillAppStateUpdateFieldsTest {

    @Test
    fun `unverified browser keeps its packageInfo instead of trusting the url`() {
        val packageInfo = PackageInfo(PackageName("com.android.chrome"), AppName("Chrome"))
        val state = AutofillAppState(
            autofillData = AutofillData(
                assistInfo = AssistInfo(
                    cluster = NodeCluster.Login.OnlyUsername(
                        username = AssistField(
                            id = TestAutofillId(1),
                            type = FieldType.Username,
                            detectionType = null,
                            value = null,
                            text = null,
                            isFocused = true,
                            nodePath = emptyList(),
                            url = null
                        )
                    ),
                    url = "https://attacker.example".toOption()
                ),
                packageInfo = packageInfo,
                isDangerousAutofill = true,
                isUnverifiedBrowser = true
            ),
            packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
        )

        val (updatedPackageInfo, updatedUrl) = state.updateAutofillFields()

        assertThat(updatedPackageInfo).isEqualTo(Some(packageInfo))
    }

    @Test
    fun `plain non-browser app with URL returns the URL`() {
        val packageInfo = PackageInfo(PackageName("com.example.app"), AppName("Example App"))
        val url = "https://example.com"
        val state = AutofillAppState(
            autofillData = AutofillData(
                assistInfo = AssistInfo(
                    cluster = NodeCluster.Login.OnlyUsername(
                        username = AssistField(
                            id = TestAutofillId(1),
                            type = FieldType.Username,
                            detectionType = null,
                            value = null,
                            text = null,
                            isFocused = true,
                            nodePath = emptyList(),
                            url = null
                        )
                    ),
                    url = url.toOption()
                ),
                packageInfo = packageInfo,
                isDangerousAutofill = false,
                isUnverifiedBrowser = false
            ),
            packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
        )

        val (updatedPackageInfo, updatedUrl) = state.updateAutofillFields()

        assertThat(updatedPackageInfo).isEqualTo(proton.android.pass.common.api.None)
        assertThat(updatedUrl).isEqualTo(url.toOption())
    }

    @Test
    fun `verified browser with URL returns the URL and discards packageInfo`() {
        val packageInfo = PackageInfo(PackageName("com.android.chrome"), AppName("Chrome"))
        val url = "https://example.com"
        val state = AutofillAppState(
            autofillData = AutofillData(
                assistInfo = AssistInfo(
                    cluster = NodeCluster.Login.OnlyUsername(
                        username = AssistField(
                            id = TestAutofillId(1),
                            type = FieldType.Username,
                            detectionType = null,
                            value = null,
                            text = null,
                            isFocused = true,
                            nodePath = emptyList(),
                            url = null
                        )
                    ),
                    url = url.toOption()
                ),
                packageInfo = packageInfo,
                isDangerousAutofill = false,
                isUnverifiedBrowser = false
            ),
            packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
        )

        val (updatedPackageInfo, updatedUrl) = state.updateAutofillFields()

        assertThat(updatedPackageInfo).isEqualTo(proton.android.pass.common.api.None)
        assertThat(updatedUrl).isEqualTo(url.toOption())
    }
}
