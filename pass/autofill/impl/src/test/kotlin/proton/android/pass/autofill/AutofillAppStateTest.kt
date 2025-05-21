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

package proton.android.pass.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.entities.AssistField
import proton.android.pass.autofill.entities.AssistInfo
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import proton.android.pass.autofill.heuristics.NodeCluster
import proton.android.pass.common.api.None
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

class AutofillAppStateTest {

    @Test
    fun `NodeCluster Empty returns isValid false`() {
        val state = AutofillAppState(
            autofillData = AutofillData(
                assistInfo = AssistInfo(
                    cluster = NodeCluster.Empty,
                    url = None
                ),
                packageInfo = PackageInfo(PackageName("some.example"), AppName("Some example")),
                isDangerousAutofill = false
            ),
            packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
        )
        assertThat(state.isValid()).isFalse()
    }

    @Test
    fun `NodeCluster not being NotEmpty returns isValid true`() {
        val state = AutofillAppState(
            autofillData = AutofillData(
                assistInfo = AssistInfo(
                    cluster = NodeCluster.Login.OnlyUsername(
                        username = AssistField(
                            id = TestAutofillId(234),
                            type = FieldType.Username,
                            detectionType = null,
                            value = null,
                            text = null,
                            isFocused = true,
                            nodePath = emptyList(),
                            url = null
                        )
                    ),
                    url = None
                ),
                packageInfo = PackageInfo(PackageName("some.example"), AppName("Some example")),
                isDangerousAutofill = false
            ),
            packageNameUrlSuggestionAdapter = PackageNameUrlSuggestionAdapterImpl()
        )
        assertThat(state.isValid()).isTrue()
    }

}
