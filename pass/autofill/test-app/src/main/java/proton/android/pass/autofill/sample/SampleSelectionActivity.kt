/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.autofillmanageractivity.AutofillManagerLoginActivity
import proton.android.pass.autofill.sample.changepassword.ChangePasswordActivity
import proton.android.pass.autofill.sample.creditcardactivity.CreditCardActivity
import proton.android.pass.autofill.sample.databinding.ActivitySampleSelectionBinding
import proton.android.pass.autofill.sample.multistep.MultiStepFirstLoginActivity
import proton.android.pass.autofill.sample.otp.OtpActivity
import proton.android.pass.autofill.sample.passkeys.PasskeysActivity
import proton.android.pass.autofill.sample.passwordcredentials.PasswordCredentialsActivity
import proton.android.pass.autofill.sample.personalinfoactivity.PersonalInfoActivity
import proton.android.pass.autofill.sample.simpleactivity.SimpleLoginActivity
import proton.android.pass.autofill.sample.simplecompose.SimpleComposeLoginActivity
import proton.android.pass.autofill.sample.simplefragment.SimpleFragmentLoginActivity
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat
import proton.android.pass.autofill.sample.webview.WebViewLoginActivity

class SampleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySampleSelectionBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.simpleActivityLoginItem.setOnClickListener { openActivity(SimpleLoginActivity::class.java) }
        binding.autofillManagerLoginItem.setOnClickListener { openActivity(AutofillManagerLoginActivity::class.java) }
        binding.multiStepLoginItem.setOnClickListener { openActivity(MultiStepFirstLoginActivity::class.java) }
        binding.simpleFragmentLoginItem.setOnClickListener { openActivity(SimpleFragmentLoginActivity::class.java) }
        binding.simpleComposeLoginItem.setOnClickListener { openActivity(SimpleComposeLoginActivity::class.java) }
        binding.changePasswordItem.setOnClickListener { openActivity(ChangePasswordActivity::class.java) }
        binding.otpItem.setOnClickListener { openActivity(OtpActivity::class.java) }
        binding.creditCardItem.setOnClickListener { openActivity(CreditCardActivity::class.java) }
        binding.personalInfoItem.setOnClickListener { openActivity(PersonalInfoActivity::class.java) }
        binding.webviewLoginItem.setOnClickListener { openActivity(WebViewLoginActivity::class.java) }
        binding.passkeysLoginItem.setOnClickListener { openActivity(PasskeysActivity::class.java) }
        binding.passwordCredentialsItem.setOnClickListener { openActivity(PasswordCredentialsActivity::class.java) }

        setContentView(binding.root)
    }

    private fun <T : Activity> openActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }
}
