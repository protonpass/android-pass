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

import org.junit.Test
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext

class OtherAutofillTest : BaseAutofillTest() {

    @Test
    fun `do not autofill anything on protonmail composer`() {
        runOtherAutofillTest(
            file = "other/app_protonmail_composer.json",
            item = loginItem(),
            allowEmptyFields = true
        )
    }

    @Test
    fun `do not autofill anything on wallapop message`() {
        runOtherAutofillTest(
            file = "other/app_wallapop_message.json",
            item = loginItem(),
            allowEmptyFields = true
        )
    }

    @Test
    fun `do not autofill anything on linkedin message`() {
        runOtherAutofillTest(
            file = "other/app_linkedin_message.json",
            item = loginItem(),
            allowEmptyFields = true
        )
    }

    @Test
    fun `do not autofill anything on mastodon new post`() {
        runOtherAutofillTest(
            file = "other/app_org.joinmastodon.android_composer.json",
            item = loginItem(),
            allowEmptyFields = true
        )
    }

    private fun loginItem(): AutofillItem = AutofillItem.Login(
        itemId = "123",
        shareId = "123",
        username = ExpectedAutofill.USERNAME.value,
        email = ExpectedAutofill.EMAIL.value,
        password = FakeEncryptionContext.encrypt(ExpectedAutofill.PASSWORD.value),
        totp = null,
        shouldLinkPackageName = false
    )

    private fun runOtherAutofillTest(
        file: String,
        item: AutofillItem,
        allowEmptyFields: Boolean,
        flags: List<RequestFlags> = emptyList()
    ) {
        runAutofillTest(
            file = file,
            item = item,
            requestFlags = flags,
            allowEmptyFields = allowEmptyFields
        )
    }
}
