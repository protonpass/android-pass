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

class IdentityAutofillTest : BaseAutofillTest() {

    @Test
    fun `can autofill amazon app`() {
        runIdentityAutofillTest("identity/app_com.amazon.mShop.android.shopping.json")
    }

    @Test
    fun `can autofill ebay app`() {
        runIdentityAutofillTest("identity/app_com.ebay.mobile.json")
    }

    @Test
    fun `can autofill a random shopify site chrome`() {
        runIdentityAutofillTest("identity/chrome_shopify_random.json")
    }

    @Test
    fun `can autofill shopify chrome`() {
        runIdentityAutofillTest("identity/chrome_shopify.json")
    }

    @Test
    fun `can autofill vinted chrome en`() {
        runIdentityAutofillTest("identity/chrome_vinted_en.json")
    }

    @Test
    fun `can autofill vinted chrome fr`() {
        runIdentityAutofillTest("identity/chrome_vinted_fr.json")
    }

    @Test
    fun `can autofill paypal chrome en`() {
        runIdentityAutofillTest("identity/chrome_paypal_en.json")
    }

    @Test
    fun `can autofill paypal chrome es`() {
        runIdentityAutofillTest("identity/chrome_paypal_es.json")
    }

    @Test
    fun `can autofill fill dev chrome`() {
        runIdentityAutofillTest("identity/chrome_fill_dev.json")
    }

    @Test
    fun `can autofill chrome address es`() {
        runIdentityAutofillTest("identity/chrome_address_es.json")
    }

    @Test
    fun `can autofill chrome address en`() {
        runIdentityAutofillTest("identity/chrome_address_en.json")
    }

    @Test
    fun `can autofill chrome address fr`() {
        runIdentityAutofillTest("identity/chrome_address_fr.json")
    }

    @Test
    fun `can autofill chrome proton contact`() {
        runIdentityAutofillTest("identity/chrome_proton_contact.json")
    }

    @Test
    fun `can autofill wallapop address`() {
        runIdentityAutofillTest("identity/app_com.wallapop.com_address.json")
    }

    private fun runIdentityAutofillTest(file: String) {
        runAutofillTest(
            file = file,
            item = AutofillItem.Identity(
                itemId = "itemID",
                shareId = "shareID",
                fullName = ExpectedAutofill.IDENTITY_FULL_NAME.value,
                firstName = ExpectedAutofill.IDENTITY_FIRST_NAME.value,
                middleName = ExpectedAutofill.IDENTITY_MIDDLE_NAME.value,
                lastName = ExpectedAutofill.IDENTITY_LAST_NAME.value,
                address = ExpectedAutofill.IDENTITY_ADDRESS.value,
                city = ExpectedAutofill.IDENTITY_CITY.value,
                phoneNumber = ExpectedAutofill.IDENTITY_PHONE.value,
                postalCode = ExpectedAutofill.IDENTITY_POSTAL_CODE.value,
                organization = ExpectedAutofill.IDENTITY_ORGANIZATION.value,
                country = ExpectedAutofill.IDENTITY_COUNTRY.value,
                userId = "userID"
            )
        )
    }

}
