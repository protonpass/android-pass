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
import proton.android.pass.crypto.fakes.context.TestEncryptionContext

class LoginAutofillTest : BaseAutofillTest() {

    @Test
    fun `can autofill news ycombinator com firefox (focus on first username)`() {
        runLoginAutofillTest("login/firefox_news.ycombinator.com_firstusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on first password)`() {
        runLoginAutofillTest("login/firefox_news.ycombinator.com_firstpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second username)`() {
        runLoginAutofillTest("login/firefox_news.ycombinator.com_secondusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second password)`() {
        runLoginAutofillTest("login/firefox_news.ycombinator.com_secondpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on first username)`() {
        runLoginAutofillTest("login/chrome_news.ycombinator.com_firstusername.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on first password)`() {
        runLoginAutofillTest("login/chrome_news.ycombinator.com_firstpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on second username)`() {
        runLoginAutofillTest("login/chrome_news.ycombinator.com_secondusername.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on second password)`() {
        runLoginAutofillTest("login/chrome_news.ycombinator.com_secondpassword.json")
    }

    @Test
    fun `can autofill account dyn com chrome`() {
        runLoginAutofillTest("login/chrome_account.dyn.com.json")
    }

    @Test
    fun `can autofill citiretailservices citibank com chrome (focus on username)`() {
        runLoginAutofillTest("login/chrome_citiretailservices.citibank.com_username.json")
    }

    @Test
    fun `can autofill citiretailservices citibank com chrome (focus on password)`() {
        runLoginAutofillTest("login/chrome_citiretailservices.citibank.com_password.json")
    }

    @Test
    fun `can autofill protonmail app`() {
        runLoginAutofillTest("login/app_ch.protonmail.android.json")
    }

    @Test
    fun `can autofill instagram app`() {
        runLoginAutofillTest("login/app_com.instagram.android.json")
    }

    @Test
    fun `can autofill proton me duckduckgo username`() {
        runLoginAutofillTest("login/duckduckgo.proton.me.username.json")
    }

    @Test
    fun `can autofill proton me duckduckgo password`() {
        runLoginAutofillTest("login/duckduckgo.proton.me.password.json")
    }

    @Test
    fun `can autofill app com booking password`() {
        runLoginAutofillTest(
            "login/app_com.booking.android_password.json",
            listOf(RequestFlags.FLAG_MANUAL_REQUEST)
        )
    }

    @Test
    fun `can autofill app com booking repeat password`() {
        runLoginAutofillTest(
            "login/app_com.booking.android_repeatpassword.json",
            listOf(RequestFlags.FLAG_MANUAL_REQUEST)
        )
    }

    @Test
    fun `can autofill account xiaomi com chrome username`() {
        runLoginAutofillTest("login/chrome_account.xiaomi.com_username.json")
    }

    @Test
    fun `can autofill login klm com chrome username`() {
        runLoginAutofillTest("login/chrome_login.klm.com_username.json")
    }

    @Test
    fun `can autofill store steampowered com chrome username`() {
        runLoginAutofillTest("login/chrome_store.steampowered.com_username.json")
    }

    @Test
    fun `can autofill cibc app where username is credit card number`() {
        runLoginAutofillTest("login/app_cibc_username_is_ccnum.json")
    }

    @Test
    fun `can autofill autospill sample focusing on the app fields`() {
        runLoginAutofillTest("other/app_autofillsample_autospill_app_focus.json")
    }

    @Test
    fun `can autofill autospill sample focusing on the web fields`() {
        runLoginAutofillTest("other/app_autofillsample_autospill_web_focus.json")
    }

    @Test
    fun `can autofill basic login form in firefox`() {
        runLoginAutofillTest("login/firefox_basic_login_site.json")
    }

    @Test
    fun `can autofill login ebay app`() {
        runLoginAutofillTest("login/app_com.ebay.mobile.json")
    }

    @Test
    fun `can autofill login discord app`() {
        runLoginAutofillTest("login/app_com.discord_login.json")
    }

    @Test
    fun `does not autofill messages in discord app`() {
        runLoginAutofillTest("login/app_com.discord_messages.json", allowEmptyFields = true)
    }

    @Test
    fun `can autofill difmark_com sign up in chrome`() {
        runLoginAutofillTest("login/chrome_difmark.com_signup.json")
    }

    private fun runLoginAutofillTest(
        file: String,
        flags: List<RequestFlags> = emptyList(),
        allowEmptyFields: Boolean = false
    ) {
        runAutofillTest(
            file = file,
            item = AutofillItem.Login(
                itemId = "123",
                shareId = "123",
                username = ExpectedAutofill.USERNAME.value,
                email = ExpectedAutofill.EMAIL.value,
                password = TestEncryptionContext.encrypt(ExpectedAutofill.PASSWORD.value),
                totp = null,
                shouldLinkPackageName = false,
                userId = "userID"
            ),
            requestFlags = flags,
            allowEmptyFields = allowEmptyFields
        )
    }
}
