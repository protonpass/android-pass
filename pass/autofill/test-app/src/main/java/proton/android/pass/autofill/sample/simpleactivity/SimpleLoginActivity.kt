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

package proton.android.pass.autofill.sample.simpleactivity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.LoginResultActivity
import proton.android.pass.autofill.sample.databinding.ActivitySimpleLoginBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

class SimpleLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySimpleLoginBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginResultActivity::class.java))
            finish()
        }

        binding.showPasswordButton.setOnClickListener {
            if (binding.passwordEditText.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT
            } else {
                binding.passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

        setContentView(binding.root)
    }
}
