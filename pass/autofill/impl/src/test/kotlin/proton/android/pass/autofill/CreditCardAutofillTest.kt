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

class CreditCardAutofillTest : BaseAutofillTest() {

    @Test
    fun `can autofill aliexpress com chrome`() {
        runCCAutofillTest("creditcard/chrome_aliexpress_credit_card.json")
    }

    @Test
    fun `can autofill instant gaming chrome`() {
        runCCAutofillTest("creditcard/chrome_instant_gaming_creditcard.json")
    }

    @Test
    fun `can autofill amazon app`() {
        runCCAutofillTest("creditcard/app_amazon_new_credit_card.json")
    }

    @Test
    fun `can autofill kiwoko chrome`() {
        runCCAutofillTest("creditcard/chrome_kiwoko_credit_card.json")
    }

    @Test
    fun `can autofill paddle chrome`() {
        runCCAutofillTest("creditcard/chrome_paddle_credit_card.json")
    }

    @Test
    fun `can autofill pingponx com chrome`() {
        runCCAutofillTest("creditcard/chrome_pingpongx_com_creditcard.json")
    }

    @Test
    fun `can autofill proton chrome`() {
        runCCAutofillTest("creditcard/chrome_proton_new_credit_card.json")
    }

    @Test
    fun `can autofill redsys chrome`() {
        runCCAutofillTest("creditcard/chrome_redsys_credit_card.json")
    }

    @Test
    fun `can autofill stripe chrome`() {
        runCCAutofillTest("creditcard/chrome_stripe_credit_card.json")
    }

    @Test
    fun `can autofill zooplus chrome`() {
        runCCAutofillTest("creditcard/chrome_zooplus_credit_card.json")
    }

    private fun runCCAutofillTest(file: String) {
        runAutofillTest(
            file = file,
            item = AutofillItem.CreditCard(
                number = ExpectedAutofill.CC_NUMBER.value,
                cardHolder = ExpectedAutofill.CC_CARDHOLDER_NAME.assertedValue,
                expiration = CC_EXPIRATION,
                cvv = FakeEncryptionContext.encrypt(ExpectedAutofill.CC_CVV.value),
                itemId = "itemID",
                shareId = "shareID"
            )
        )
    }

}
