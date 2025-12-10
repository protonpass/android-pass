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

package proton.android.pass.autofill.sample.creditcardactivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.LoginResultActivity
import proton.android.pass.autofill.sample.databinding.ActivityCreditcardBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

@Suppress("MagicNumber")
class CreditCardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityCreditcardBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.nameEditText.setAutofillHints(View.AUTOFILL_HINT_NAME)
        binding.numberEditText.setAutofillHints(View.AUTOFILL_HINT_CREDIT_CARD_NUMBER)
        binding.cvcEditText.setAutofillHints(View.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE)
        binding.monthPicker.setAutofillHints(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH)
        binding.yearPicker.setAutofillHints(View.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR)
        binding.monthPicker.minValue = 1
        binding.monthPicker.maxValue = 12
        binding.yearPicker.minValue = 1900
        binding.yearPicker.maxValue = 2100
        binding.payButton.setOnClickListener {
            startActivity(Intent(this, LoginResultActivity::class.java))
            finish()
        }

        setContentView(binding.root)
    }
}
