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

package proton.android.pass.autofill.sample.multistep

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.autofill.AutofillManager
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.databinding.ActivityMultistepLoginFirstBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

class MultiStepFirstLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMultistepLoginFirstBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, MultiStepSecondLoginActivity::class.java))
            finish()
        }
        val autofillManager = getSystemService(AutofillManager::class.java)

        binding.loginEditText.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                autofillManager.requestAutofill(v)
            }
        }

        setContentView(binding.root)
    }
}
