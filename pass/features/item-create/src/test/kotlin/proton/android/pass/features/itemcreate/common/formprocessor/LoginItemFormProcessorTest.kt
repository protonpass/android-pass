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

package proton.android.pass.features.itemcreate.common.formprocessor

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonuimodels.api.UIAutofillUrl
import proton.android.pass.commonuimodels.api.passwords.PasswordChecksUiState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.features.itemcreate.common.LoginItemValidationError
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.totp.fakes.FakeTotpManager

internal class LoginItemFormProcessorTest {

    private lateinit var processor: LoginItemFormProcessor

    @Before
    fun setUp() {
        val totpManager = FakeTotpManager()
        processor = LoginItemFormProcessor(
            primaryTotpFormProcessor = PrimaryTotpFormProcessor(totpManager),
            customFieldFormProcessor = UICustomFieldContentFormProcessor(totpManager)
        )
    }

    @Test
    fun `wildcard url with pattern mode is preserved`() = runTest {
        val result = process(UIAutofillUrl(url = WILDCARD_URL, mode = AutofillUrlMode.Pattern))

        assertThat(result).isInstanceOf(FormProcessingResult.Success::class.java)
        assertThat((result as FormProcessingResult.Success).sanitized.urls).containsExactly(WILDCARD_URL)
    }

    @Test
    fun `regex url with regular expression mode is preserved`() = runTest {
        val result = process(UIAutofillUrl(url = REGEX_URL, mode = AutofillUrlMode.RegularExpression))

        assertThat(result).isInstanceOf(FormProcessingResult.Success::class.java)
        assertThat((result as FormProcessingResult.Success).sanitized.urls).containsExactly(REGEX_URL)
    }

    @Test
    fun `wildcard url with default mode is invalid`() = runTest {
        val result = process(UIAutofillUrl(url = WILDCARD_URL, mode = AutofillUrlMode.Default))

        assertThat(result).isEqualTo(invalidUrlAt(0))
    }

    @Test
    fun `regex url with default mode is invalid`() = runTest {
        val result = process(UIAutofillUrl(url = REGEX_URL, mode = AutofillUrlMode.Default))

        assertThat(result).isEqualTo(invalidUrlAt(0))
    }

    @Test
    fun `wildcard url without autofill url entry is invalid`() = runTest {
        val result = process(urls = listOf(WILDCARD_URL))

        assertThat(result).isEqualTo(invalidUrlAt(0))
    }

    @Test
    fun `default urls are still sanitized alongside pattern urls`() = runTest {
        val result = process(
            UIAutofillUrl(url = WILDCARD_URL, mode = AutofillUrlMode.Pattern),
            UIAutofillUrl(url = "example.org", mode = AutofillUrlMode.Default)
        )

        assertThat(result).isInstanceOf(FormProcessingResult.Success::class.java)
        assertThat((result as FormProcessingResult.Success).sanitized.urls)
            .containsExactly(WILDCARD_URL, "https://example.org")
            .inOrder()
    }

    private suspend fun process(vararg autofillUrls: UIAutofillUrl) = process(
        urls = autofillUrls.map(UIAutofillUrl::url),
        autofillUrls = autofillUrls.toList()
    )

    private suspend fun process(
        urls: List<String>,
        autofillUrls: List<UIAutofillUrl> = emptyList()
    ): FormProcessingResult<LoginItemFormState> = processor.process(
        input = LoginItemFormProcessor.Input(
            originalPrimaryTotp = None,
            originalCustomFields = emptyList(),
            formState = formState(urls = urls, autofillUrls = autofillUrls)
        ),
        decrypt = FakeEncryptionContext::decrypt,
        encrypt = FakeEncryptionContext::encrypt
    )

    private fun invalidUrlAt(index: Int) =
        FormProcessingResult.Error<LoginItemFormState>(setOf(LoginItemValidationError.InvalidUrl(index)))

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
        const val REGEX_URL = "^https://(www\\.)?example\\.com/.*$"
    }
}
