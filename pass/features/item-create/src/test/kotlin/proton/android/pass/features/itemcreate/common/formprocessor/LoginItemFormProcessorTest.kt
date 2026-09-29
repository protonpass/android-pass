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
import proton.android.pass.common.api.ItemIcon
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.features.itemcreate.common.ItemIconValidationError
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.totp.fakes.FakeTotpManager

class LoginItemFormProcessorTest {

    private lateinit var instance: LoginItemFormProcessor

    @Before
    fun setUp() {
        val totpManager = FakeTotpManager()
        instance = LoginItemFormProcessor(
            primaryTotpFormProcessor = PrimaryTotpFormProcessor(totpManager),
            customFieldFormProcessor = UICustomFieldContentFormProcessor(totpManager)
        )
    }

    @Test
    fun `keeps a valid icon`() = runTest {
        val result = process(icon = VALID_ICON)

        assertThat(result).isInstanceOf(FormProcessingResult.Success::class.java)
        assertThat((result as FormProcessingResult.Success).sanitized.icon).isEqualTo(VALID_ICON)
    }

    @Test
    fun `clears a blank icon`() = runTest {
        listOf("", " ").forEach { blank ->
            val result = process(icon = blank)

            assertThat(result).isInstanceOf(FormProcessingResult.Success::class.java)
            assertThat((result as FormProcessingResult.Success).sanitized.icon).isNull()
        }
    }

    @Test
    fun `blocks saving an invalid icon`() = runTest {
        listOf(
            "https://example.com/icon.png",
            "data:image/gif;base64,QUJD",
            // Declared as PNG but not a PNG
            "data:image/png;base64,QUJD",
            ItemIcon.encode(ItemIcon.SVG_MIME_TYPE, "<svg><image href=\"#a\"/></svg>".toByteArray())
        ).forEach { icon ->
            val result = process(icon = icon)

            assertThat(result).isEqualTo(FormProcessingResult.Error<LoginItemFormState>(setOf(ItemIconValidationError.Invalid)))
        }
    }

    @Test
    fun `blocks saving an icon that is too large`() = runTest {
        val icon = "data:image/png;base64," + "A".repeat(ItemIcon.MAX_LENGTH)

        val result = process(icon = icon)

        assertThat(result).isEqualTo(FormProcessingResult.Error<LoginItemFormState>(setOf(ItemIconValidationError.TooLarge)))
    }

    private suspend fun process(icon: String?) = instance.process(
        input = LoginItemFormProcessor.Input(
            originalPrimaryTotp = None,
            originalCustomFields = emptyList(),
            formState = LoginItemFormState.default(FakeEncryptionContext).copy(title = "title", icon = icon)
        ),
        decrypt = FakeEncryptionContext::decrypt,
        encrypt = FakeEncryptionContext::encrypt
    )

    private companion object {
        const val VALID_ICON = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk" +
            "+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    }
}
