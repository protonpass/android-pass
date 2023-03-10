package proton.android.pass.autofill.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.databinding.ActivitySampleSelectionBinding
import proton.android.pass.autofill.sample.simpleactivity.SimpleLoginActivity
import proton.android.pass.autofill.sample.simplecompose.SimpleComposeLoginActivity
import proton.android.pass.autofill.sample.simplefragment.SimpleFragmentLoginActivity
import proton.android.pass.autofill.sample.webview.WebViewLoginActivity

class SampleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySampleSelectionBinding.inflate(layoutInflater)

        binding.simpleActivityLoginButton.setOnClickListener {
            openActivity(SimpleLoginActivity::class.java)
        }

        binding.simpleFragmentLoginButton.setOnClickListener {
            openActivity(SimpleFragmentLoginActivity::class.java)
        }

        binding.simpleComposeLoginButton.setOnClickListener {
            openActivity(SimpleComposeLoginActivity::class.java)
        }

        binding.webviewLoginButton.setOnClickListener {
            openActivity(WebViewLoginActivity::class.java)
        }

        setContentView(binding.root)
    }

    private fun <T : Activity> openActivity(clazz: Class<T>) {
        startActivity(Intent(this, clazz))
    }
}
