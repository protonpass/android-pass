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

class LoginAutofillTest : BaseAutofillTest() {

    @Test
    fun `can autofill news ycombinator com firefox (focus on first username)`() {
        runAutofillTest("login/firefox_news.ycombinator.com_firstusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on first password)`() {
        runAutofillTest("login/firefox_news.ycombinator.com_firstpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second username)`() {
        runAutofillTest("login/firefox_news.ycombinator.com_secondusername.json")
    }

    @Test
    fun `can autofill news ycombinator com firefox (focus on second password)`() {
        runAutofillTest("login/firefox_news.ycombinator.com_secondpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on first username)`() {
        runAutofillTest("login/chrome_news.ycombinator.com_firstusername.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on first password)`() {
        runAutofillTest("login/chrome_news.ycombinator.com_firstpassword.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on second username)`() {
        runAutofillTest("login/chrome_news.ycombinator.com_secondusername.json")
    }

    @Test
    fun `can autofill news ycombinator com chrome (focus on second password)`() {
        runAutofillTest("login/chrome_news.ycombinator.com_secondpassword.json")
    }

    @Test
    fun `can autofill account dyn com chrome`() {
        runAutofillTest("login/chrome_account.dyn.com.json")
    }

    @Test
    fun `can autofill citiretailservices citibank com chrome (focus on username)`() {
        runAutofillTest("login/chrome_citiretailservices.citibank.com_username.json")
    }

    @Test
    fun `can autofill citiretailservices citibank com chrome (focus on password)`() {
        runAutofillTest("login/chrome_citiretailservices.citibank.com_password.json")
    }

    @Test
    fun `can autofill protonmail app`() {
        runAutofillTest("login/app_ch.protonmail.android.json")
    }

    @Test
    fun `can autofill instagram app`() {
        runAutofillTest("login/app_com.instagram.android.json")
    }

    @Test
    fun `can autofill proton me duckduckgo username`() {
        runAutofillTest("login/duckduckgo.proton.me.username.json")
    }

    @Test
    fun `can autofill proton me duckduckgo password`() {
        runAutofillTest("login/duckduckgo.proton.me.password.json")
    }

    @Test
    fun `can autofill app com booking password`() {
        runAutofillTest("login/app_com.booking.android_password.json", listOf(RequestFlags.FLAG_MANUAL_REQUEST))
    }

    @Test
    fun `can autofill app com booking repeat password`() {
        runAutofillTest("login/app_com.booking.android_repeatpassword.json", listOf(RequestFlags.FLAG_MANUAL_REQUEST))
    }

    @Test
    fun `can autofill account xiaomi com chrome username`() {
        runAutofillTest("login/chrome_account.xiaomi.com_username.json")
    }

    @Test
    fun `can autofill login klm com chrome username`() {
        runAutofillTest("login/chrome_login.klm.com_username.json")
    }

    @Test
    fun `can autofill store steampowered com chrome username`() {
        runAutofillTest("login/chrome_store.steampowered.com_username.json")
    }
}
