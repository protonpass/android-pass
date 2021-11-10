package me.proton.core.pass.autofill.sample.simple_activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import me.proton.core.pass.autofill.sample.LoginResultActivity
import me.proton.core.pass.autofill.sample.databinding.ActivitySimpleLoginBinding

class SimpleLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySimpleLoginBinding.inflate(layoutInflater)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginResultActivity::class.java))
            finish()
        }

        setContentView(binding.root)
    }
}
