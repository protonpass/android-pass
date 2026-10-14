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

package proton.android.pass.features.itemcreate.login

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.commonuimodels.api.UIAutofillUrl
import proton.android.pass.commonuimodels.api.passwords.PasswordChecksUiState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.AutofillUrl
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.features.itemcreate.common.UIHiddenState

internal class LoginItemFormStateTest {

    @Test
    fun `blank url in the middle keeps later autofill url modes aligned`() {
        val formState = formState(
            urls = listOf("https://a.com", "", WILDCARD_URL),
            autofillUrls = listOf(
                UIAutofillUrl(url = "https://a.com", mode = AutofillUrlMode.Default),
                UIAutofillUrl(url = "", mode = AutofillUrlMode.Default),
                UIAutofillUrl(url = WILDCARD_URL, mode = AutofillUrlMode.Pattern)
            )
        )

        val contents = formState.toItemContents(FakeEmailValidator())

        assertThat(contents.autofillUrls).containsExactly(
            AutofillUrl(url = "https://a.com", mode = AutofillUrlMode.Default),
            AutofillUrl(url = WILDCARD_URL, mode = AutofillUrlMode.Pattern)
        ).inOrder()
        assertThat(contents.urls).containsExactly("https://a.com")
    }

    private fun formState(urls: List<String>, autofillUrls: List<UIAutofillUrl>) = LoginItemFormState(
        title = "Title",
        note = "",
        email = "",
        username = "",
        password = UIHiddenState.Empty(FakeEncryptionContext.encrypt("")),
        passwordStrength = PasswordStrength.None,
        passwordChecks = PasswordChecksUiState.Initial,
        urls = urls,
        packageInfoSet = emptySet(),
        primaryTotp = UIHiddenState.Empty(FakeEncryptionContext.encrypt("")),
        customFields = emptyList(),
        passkeys = emptyList(),
        autofillUrls = autofillUrls,
        passkeyToBeGenerated = null,
        isExpandedByContent = false,
        isExpandedByUser = false,
        isExpandedByPreference = false
    )

    private companion object {
        const val WILDCARD_URL = "https://*.example.com/*"
    }
}
