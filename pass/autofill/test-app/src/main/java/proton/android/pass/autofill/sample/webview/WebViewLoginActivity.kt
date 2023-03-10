package proton.android.pass.autofill.sample.webview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.databinding.ActivityWebviewLoginBinding

class WebViewLoginActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityWebviewLoginBinding.inflate(layoutInflater)

        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
            }
            if (url == null) loadUrl("https://account.proton.black/login")
        }

        setContentView(binding.root)
    }

}
