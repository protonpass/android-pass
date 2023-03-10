package proton.android.pass.autofill.sample.simpleactivity

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.LoginResultActivity
import proton.android.pass.autofill.sample.databinding.ActivitySimpleLoginBinding

class SimpleLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySimpleLoginBinding.inflate(layoutInflater)

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
