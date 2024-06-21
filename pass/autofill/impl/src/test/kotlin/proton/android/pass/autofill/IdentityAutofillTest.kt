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
    fun `can autofill aliexpress chrome`() {
        runIdentityAutofillTest("identity/chrome_aliexpress.json")
    }

    @Test
    fun `can autofill ebay app`() {
        runIdentityAutofillTest("identity/app_com.ebay.mobile.json")
    }

    private fun runIdentityAutofillTest(file: String) {
        runAutofillTest(
            file = file,
            item = AutofillItem.Identity(
                itemId = "itemID",
                shareId = "shareID",
                fullName = ExpectedAutofill.IDENTITY_FULL_NAME.value,
                address = ExpectedAutofill.IDENTITY_ADDRESS.value,
                phoneNumber = ExpectedAutofill.IDENTITY_PHONE.value,
                postalCode = ExpectedAutofill.IDENTITY_POSTAL_CODE.value
            )
        )
    }

}
